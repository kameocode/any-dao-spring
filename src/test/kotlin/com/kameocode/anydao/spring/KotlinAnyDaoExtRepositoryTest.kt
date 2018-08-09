package com.kameocode.anydao.spring

import com.kameocode.anydao.KPage
import com.kameocode.anydao.wraps.like
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.test.context.junit4.SpringRunner
import javax.persistence.criteria.JoinType

@DataJpaTest
@RunWith(SpringRunner::class)
open class KotlinAnyDaoExtRepositoryTest {
    @Autowired
    lateinit var userRepository: UserExtRepository

    @Test
    fun testAll_ReturnDifferentEntities() {
        val u1 = UserODB(email = "email1", login = "email1", currentTodo = TodoODB(name = "taskA", importance = 9))
        val u2 = UserODB(email = "email2", login = "email2", currentTodo = TodoODB(name = "taskB", importance = 9))
        val u3 = UserODB(email = "email3", login = null, currentTodo = null)

        userRepository.saveAll(listOf(u1, u2, u3))

        val todos = userRepository.findAllBy(TodoODB::class) {
            select(it[UserODB::currentTodo])
        }
        Assert.assertEquals(todos[0].name, "taskA")
        Assert.assertEquals(todos[1].name, "taskB")
        Assert.assertEquals(2, todos.size)

        val logins = userRepository.findAllBy(String::class) {
            select(it[UserODB::login])
        }
        Assert.assertEquals(logins[0], "email1")
        Assert.assertEquals(logins[1], "email2")
        Assert.assertNull(logins[2])
    }


    @Test
    fun testAll_JoinAndReturnDifferentEntities() {
        val u1 = UserODB(email = "email1", login = "email1", currentTodo = TodoODB(name = "taskA", importance = 9))
        val u2 = UserODB(email = "email2", login = "email2", currentTodo = TodoODB(name = "taskB", importance = 9))
        val u3 = UserODB(email = "email3", login = null, currentTodo = null)

        userRepository.saveAll(listOf(u1, u2, u3))

        val todos = userRepository.findAllBy(TodoODB::class) {
            it.join(UserODB::currentTodo, JoinType.LEFT)
            select(it[UserODB::currentTodo])
        }
        Assert.assertEquals(todos[0].name, "taskA")
        Assert.assertEquals(todos[1].name, "taskB")
        Assert.assertNull(todos[2])
        Assert.assertEquals(3, todos.size)


    }

    @Test
    fun testOne_ReturnDifferentEntities() {
        val u1 = UserODB(email = "email1", login = "email1", currentTodo = TodoODB(name = "taskA", importance = 9))
        val u2 = UserODB(email = "email2", login = "email2", currentTodo = TodoODB(name = "taskB", importance = 9))
        val u3 = UserODB(email = "email3", login = null, currentTodo = null)

        userRepository.saveAll(listOf(u1, u2, u3))

        try {
            userRepository.findOneBy(TodoODB::class) {
                select(it[UserODB::currentTodo])
            }
            Assert.fail()
        } catch (err: org.springframework.dao.IncorrectResultSizeDataAccessException) {
            // org.springframework.dao.IncorrectResultSizeDataAccessException: query did not return a unique result: 2;
            // nested exception is javax.persistence.NonUniqueResultException: query did not return a unique result: 2
        }

        val todo = userRepository.findOneBy(TodoODB::class) {
            it[UserODB::email] eq "email1"
            select(it[UserODB::currentTodo])
        }
        Assert.assertNotNull(todo)

    }

    @Test
    fun testFirst_ReturnDifferentEntities() {
        val u1 = UserODB(email = "email1", login = "email1", currentTodo = TodoODB(name = "taskA", importance = 9))
        val u2 = UserODB(email = "email2", login = "email2", currentTodo = TodoODB(name = "taskB", importance = 9))
        val u3 = UserODB(email = "email3", login = null, currentTodo = null)

        userRepository.saveAll(listOf(u1, u2, u3))


        val todo = userRepository.findFirstBy(TodoODB::class) {
            select(it[UserODB::currentTodo])
        }
        Assert.assertNotNull(todo)
    }

    @Test
    fun testUpdate() {
        val u1 = UserODB(email = "email1", login = "email1", currentTodo = TodoODB(name = "taskA", importance = 9))
        val u2 = UserODB(email = "email2", login = "email2", currentTodo = TodoODB(name = "taskB", importance = 9))
        val u3 = UserODB(email = "email3", login = null, currentTodo = null)

        userRepository.saveAll(listOf(u1, u2, u3))


        val updatedCount = userRepository.updateBy {
            it[UserODB::email] = "changed"
            it[UserODB::email] like "email%"
        }
        Assert.assertEquals(3, updatedCount)
        val count2 = userRepository.countBy {
            it[UserODB::email] like "email%"
        }
        Assert.assertEquals(0, count2)

    }

    @Test
    fun testDelete() {
        val u1 = UserODB(email = "email1", login = "email1", currentTodo = TodoODB(name = "taskA", importance = 9))
        val u2 = UserODB(email = "email2", login = "email2", currentTodo = TodoODB(name = "taskB", importance = 9))
        val u3 = UserODB(email = "email3", login = null, currentTodo = null)

        userRepository.saveAll(listOf(u1, u2, u3))


        val updatedCount = userRepository.deleteBy {
            it[UserODB::email] like "email%"
        }
        Assert.assertEquals(3, updatedCount)
        val count2 = userRepository.countBy {
            it
        }
        Assert.assertEquals(0, count2)

    }

    @Test
    fun testPage() {
        val u1 = UserODB(email = "email1", login = "email1", currentTodo = TodoODB(name = "taskA", importance = 9))
        val u2 = UserODB(email = "email2", login = "email2", currentTodo = TodoODB(name = "taskB", importance = 9))
        val u3 = UserODB(email = "email3", login = null, currentTodo = null)

        userRepository.saveAll(listOf(u1, u2, u3))

        userRepository.pagesBy(String::class, KPage(2, 0)) {
            select(it[UserODB::email])
        }.forEachUntil {
            Assert.assertEquals("email1", it[0])
            Assert.assertEquals("email2", it[1])
            false
        }

    }

}

@TestComponent
interface UserExtRepository : KotlinAnyDaoExtRepository<UserODB, Long>
