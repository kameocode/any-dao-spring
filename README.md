[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.kameocode/any-dao/badge.svg)](http://repo1.maven.org/maven2/com/kameocode/any-dao-spring/1.0.2/)

# any-dao-spring #

Any-dao-spring provides an [any-dao](https://github.com/kameocode/any-dao) Kotlin extension for spring JPA repositories.

Why? to have typesafe queries which may be composed (similar to JpaSepcificationExecutor) but in pure Kotlin


Define repository:
```
@Repository
interface UserRepository : KotlinAnyDaoRepository<UserODB, Long>
```
It extends both `JpaSpecificationExecutor<ENTITY>` and `JpaRepository<ENTITY, KEY>`.

Use it like this:
```
val todos = userRepository.findAllBy(TodoODB::class) {
    it.join(UserODB::currentTodo, JoinType.LEFT)
    select(it[UserODB::currentTodo])
}
val users = userRepository.findAllBy {
    it[UserODB::email] isIn subqueryFrom(TodoODB::class) {
        it[TodoODB::importance] greaterThan 10
        select(it[TodoODB::name])
    }
}
```

The good practice is to compose complicated query with named specifications that can be copmosed one into another:

```
// specify extension functions with predicates
fun KRoot<UserODB>.hasAdminOrNormalRole() {
    this[UserODB::userRoles].isMember(setOf(UserRole.ADMIN, UserRole.NORMAL))
}
fun KRoot<UserODB>.hasActiveStatus() {
    this[UserODB::status] eq UserStatus.ACTIVE
}
fun KRoot<UserODB>.isUserAllowedToCreateTask() {
    hasAdminOrNormalRole()
    or
    hasActiveStatus()
}

...

// later in code:    
val users = userRepository.findAllBy {
    isUserAllowedToCreateTask()
}
```



 
