package com.kameocode.anydao.spring

import com.kameocode.anydao.AnyDao
import com.kameocode.anydao.KPage
import com.kameocode.anydao.KPagesResult
import com.kameocode.anydao.KQuery
import com.kameocode.anydao.KRoot
import com.kameocode.anydao.wraps.RootWrapUpdate
import org.springframework.aop.framework.Advised
import org.springframework.aop.support.AopUtils
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.io.Serializable
import javax.persistence.EntityManager
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


@NoRepositoryBean
interface KotlinAnyDaoExtRepository<ENTITY : Any, KEY : Serializable> : KotlinAnyDaoRepository<ENTITY, KEY> {

    @JvmDefault
    fun <RESULT : Any> findAllBy(resultClass: KClass<RESULT>, query: KQuery<ENTITY, RESULT>): List<RESULT> {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.all(domainClass.java, resultClass.java, query);
    }

    @JvmDefault
    fun <RESULT : Any> findAllMutableBy(resultClass: KClass<RESULT>, query: KQuery<ENTITY, RESULT>): MutableList<RESULT> {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.all(domainClass.java, resultClass.java, query) as MutableList<RESULT>
    }

    @JvmDefault
    fun <RESULT : Any> findOneBy(resultClass: KClass<RESULT>, query: KQuery<ENTITY, RESULT>): RESULT {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.one(domainClass.java, resultClass.java, query);
    }

    @JvmDefault
    fun <RESULT : Any> findFirstBy(resultClass: KClass<RESULT>, query: KQuery<ENTITY, RESULT>): RESULT? {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.first(domainClass.java, resultClass.java, query);
    }

    @JvmDefault
    fun updateBy(updateQuery: (RootWrapUpdate<ENTITY, ENTITY>) -> Unit): Int {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.update(domainClass.java, updateQuery);
    }

    @JvmDefault
    fun deleteBy(deleteQuery: (KRoot<ENTITY>) -> Unit): Int {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.delete(domainClass.java, deleteQuery);
    }

    @JvmDefault
    fun existsBy(query: KQuery<ENTITY, *>): Boolean {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.exists(domainClass.java, query);
    }

    @JvmDefault
    fun pagesBy(page: KPage = KPage(), query: KQuery<ENTITY, ENTITY>): KPagesResult<ENTITY> {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.pages(domainClass.java, domainClass.java, page, query)
    }


    @JvmDefault
    fun <RESULT : Any> pagesBy(resultClass: KClass<RESULT>, page: KPage = KPage(), query: KQuery<ENTITY, RESULT>): KPagesResult<RESULT> {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.pages(domainClass.java, resultClass.java, page, query)
    }

    @JvmDefault
    fun <NUM> pagesSortedBy(prop: KProperty1<ENTITY, NUM>, page: KPage = KPage(), query: KQuery<ENTITY, ENTITY>):
            KPagesResult<ENTITY> where NUM : Number, NUM : Comparable<NUM> {
        val (anyDao, domainClass) = fetchRequiredObjects<ENTITY>(this)
        return anyDao.pagesSorted(domainClass.java, domainClass.java, prop, page, query)
    }

}

private fun <ENTITY : Any> fetchRequiredObjects(obj: Any): Pair<AnyDao, KClass<ENTITY>> {
    val repo = if (AopUtils.isJdkDynamicProxy(obj)) {
        (obj as Advised).targetSource.target as SimpleJpaRepository<*, *>
    } else {
        obj as SimpleJpaRepository<*, *>
    }
    val field = SimpleJpaRepository::class.java.getDeclaredField("em");
    if (!field.isAccessible) {
        field.isAccessible = true
    }

    val domainClassMethod = SimpleJpaRepository::class.java.getDeclaredMethod("getDomainClass");
    if (!domainClassMethod.isAccessible) {
        domainClassMethod.isAccessible = true
    }

    val em = field.get(repo) as EntityManager
    val domainClass = (domainClassMethod.invoke(repo) as Class<ENTITY>).kotlin

    return AnyDao(em) to domainClass
}

