package com.kameocode.anydao.spring

import com.kameocode.anydao.KRoot
import com.kameocode.anydao.wraps.greaterThan
import com.kameocode.anydao.wraps.isEmpty
import com.kameocode.anydao.wraps.isMember
import com.kameocode.anydao.wraps.like
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit4.SpringRunner
import javax.persistence.criteria.JoinType

@DataJpaTest
@RunWith(SpringRunner::class)
open class KotlinAnyDaoRepositoryTest {


    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun testFindAllByWithSubquery() {
        val u1 = UserODB(email = "email1", todos = listOf(
                TodoODB(name = "task1.1", importance = 9)))
        val u2 = UserODB(email = "email2", todos = listOf(
                TodoODB(name = "task2.1", importance = 20)))
        val u3 = UserODB(email = "email3", todos = listOf(
                TodoODB(name = "email2", importance = 30)))
        val u4 = UserODB(email = "email4", todos = listOf(
                TodoODB(name = "email1", importance = 50)))
        userRepository.saveAll(listOf(u1, u2, u3, u4))

        val users = userRepository.findAllBy {
            it[UserODB::email] isIn subqueryFrom(TodoODB::class) {
                it[TodoODB::importance] greaterThan 10
                select(it[TodoODB::name])
            }
        }

        Assert.assertEquals(listOf("email1", "email2"), users.map { it.email })
    }

    @Test
    fun testFindAllByWithJoin() {
        val u1 = UserODB(email = "email1", todos = listOf(
                TodoODB(name = "task1.1", importance = 9)))
        val u2 = UserODB(email = "email2", todos = listOf(
                TodoODB(name = "task2.2", importance = 20)))
        val u3 = UserODB(email = "email3", todos = listOf(
                TodoODB(name = "task3.2", importance = 30)))
        val u4 = UserODB(email = "email4", todos = listOf())
        userRepository.saveAll(listOf(u1, u2, u3, u4))

        val users = userRepository.findAllBy {
            val join = it.joinList(UserODB::todos, JoinType.LEFT)
            join[TodoODB::name] like "task1.%"
            or
            it[UserODB::todos].isEmpty()
        }

        Assert.assertEquals(listOf("email1", "email4"), users.map { it.email })


        val users2 = userRepository.findAllBy {
            val join = it.joinList(UserODB::todos)
            join[TodoODB::name] like "task1.%"
            or
            it[UserODB::todos].isEmpty()
        }

        Assert.assertEquals(listOf("email1"), users2.map { it.email })
    }


    @Test
    fun testFindAllBy() {
        val u1 = UserODB(email = "email1", todos = listOf(
                TodoODB(name = "task1.1")))
        val u2 = UserODB(email = "email2", todos = listOf(
                TodoODB(name = "task2.1")))
        val u3 = UserODB(email = "email3", todos = listOf(
                TodoODB(name = "task3.1")))
        userRepository.saveAll(listOf(u1, u2, u3))

        val users = userRepository.findAllBy { it[UserODB::email] like "%il1" }

        Assert.assertEquals("email1", users.first().email)
        Assert.assertEquals(1, users.size)
    }


    @Test
    fun testFindAllBy_WithExternalSpecification() {
        val u1 = UserODB(email = "email1", userRoles = setOf(UserRole.ADMIN), todos = listOf(
                TodoODB(name = "task1.1")))
        val u2 = UserODB(email = "email2", userRoles = setOf(UserRole.ADMIN, UserRole.GUEST), todos = listOf(
                TodoODB(name = "task2.1")))
        val u3 = UserODB(email = "email3", userRoles = setOf(UserRole.NORMAL), todos = listOf(
                TodoODB(name = "task3.1")))
        userRepository.saveAll(listOf(u1, u2, u3))

        val users = userRepository.findAllBy {
            isUserAllowedToCreateTask()
        }

        Assert.assertEquals(listOf("email1", "email2"), users.map { it.email })
        Assert.assertEquals(2, users.size)
    }


    fun KRoot<UserODB>.isUserAnAdmin() {
        this[UserODB::userRoles].isMember(setOf(UserRole.ADMIN))
    }

    fun KRoot<UserODB>.isUserAGuest() {
        this[UserODB::userRoles].isMember(setOf(UserRole.GUEST))
    }
    fun KRoot<UserODB>.isUserAllowedToCreateTask() {
        this.isUserAGuest()
        or
        this.isUserAnAdmin()
    }


    @Test
    fun testFindAllByWithSort() {
        val u1 = UserODB(login = "aaa", email = "email1", todos = listOf(
                TodoODB(name = "task1.1")))
        val u2 = UserODB(login = "bbb", email = "xemail2", todos = listOf(
                TodoODB(name = "task2.1")))
        val u3 = UserODB(login = "ccc", email = "email3", todos = listOf(
                TodoODB(name = "task3.1")))
        userRepository.saveAll(listOf(u1, u2, u3))

        val users = userRepository.findAllBy(Sort.by(UserODB::login.name)) { it[UserODB::email] like "email%" }
        Assert.assertEquals(listOf("aaa", "ccc"), users.map { it.login })

        val users2 = userRepository.findAllBy(Sort.by(Sort.Direction.DESC, UserODB::login.name)) { it[UserODB::email] like "email%" }
        Assert.assertEquals(listOf("ccc", "aaa"), users2.map { it.login })
    }

    @Test
    fun testFindAllByWithPageable() {

        for (i in 1..9) {
            userRepository.save(UserODB(login = "login$i", email = "email$i"))
        }

        val users = userRepository.findAllBy(PageRequest.of(0, 2)) { it[UserODB::email] like "email%" }
        Assert.assertEquals(listOf("login1", "login2"), users.content.map { it.login })

        val users2 = userRepository.findAllBy(PageRequest.of(1, 2)) { it[UserODB::email] like "email%" }
        Assert.assertEquals(listOf("login3", "login4"), users2.content.map { it.login })

        val users3 = userRepository.findAllBy(PageRequest.of(0, 2, Sort.Direction.DESC, UserODB::login.name)) { it[UserODB::email] like "email%" }
        Assert.assertEquals(listOf("login9", "login8"), users3.content.map { it.login })

    }

    @Test
    fun testFindOneBy() {
        val u1 = UserODB(email = "email1", todos = listOf(
                TodoODB(name = "task1.1")))
        val u2 = UserODB(email = "email2", todos = listOf(
                TodoODB(name = "task2.1")))
        val u3 = UserODB(email = "email3", todos = listOf(
                TodoODB(name = "task3.1")))
        userRepository.saveAll(listOf(u1, u2, u3))

        val user = userRepository.findOneBy { it[UserODB::email] like "%il1" }

        Assert.assertNotNull(user)
        Assert.assertEquals("email1", user!!.email)


        val user2 = userRepository.findOneBy { it[UserODB::email] like "not existing email" }
        Assert.assertNull(user2)
    }


    @Test
    fun testCountBy() {
        val u1 = UserODB(email = "email1", todos = listOf(
                TodoODB(name = "task1.1")))
        val u2 = UserODB(email = "email2", todos = listOf(
                TodoODB(name = "task2.1")))
        val u3 = UserODB(email = "xemail3", todos = listOf(
                TodoODB(name = "task3.1")))
        userRepository.saveAll(listOf(u1, u2, u3))

        val count = userRepository.countBy { it[UserODB::email] like "email%" }
        Assert.assertEquals(2, count)


        val count2 = userRepository.countBy { it[UserODB::email] like "not existing email" }
        Assert.assertEquals(0, count2)
    }
}

@TestComponent
interface UserRepository : KotlinAnyDaoRepository<UserODB, Long>