package com.kameocode.anydao.spring

import java.time.LocalDateTime
import java.util.Collections.emptyList
import javax.persistence.CascadeType
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

enum class UserRole {
    ADMIN,
    NORMAL,
    GUEST
}

@Entity
data class UserODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   val id: Long = 0,

                   val email: String,
                   var login: String? = null,

                   @ElementCollection
                   val userRoles: Set<UserRole> = emptySet(),

                   @OneToMany(cascade = [CascadeType.ALL])
                   var todos: List<TodoODB> = emptyList(),

                   @ManyToOne(cascade = [CascadeType.ALL])
                   var address: AddressODB? = null,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   val currentTodo: TodoODB? = null,

                   @ManyToOne(cascade = [CascadeType.ALL])
                   val currentTodoNotNull: TodoODB = TodoODB(name="test"),

                   val createDateTime: LocalDateTime = LocalDateTime.now()
)

@Entity
data class AddressODB(@Id
                      @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                      val id: Long = 0,
                      val city: String,
                      val country: String)

@Entity
data class TodoODB(@Id
                   @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
                   val id: Long = 0,
                   val importance: Int = 0,

                   val name: String,

                   val createDateTime: LocalDateTime = LocalDateTime.now(),
                   val executionDateTime: LocalDateTime? = null
)


