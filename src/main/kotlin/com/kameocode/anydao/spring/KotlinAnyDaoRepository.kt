package com.kameocode.anydao.spring

import com.kameocode.anydao.AnyDao
import com.kameocode.anydao.KSelect
import com.kameocode.anydao.wraps.RootWrap
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import java.io.Serializable
import javax.persistence.criteria.CriteriaQuery

typealias KSpecification<E> = RootWrap<E, E>.(RootWrap<E, E>) -> Unit


@NoRepositoryBean
interface KotlinAnyDaoRepository<ENTITY : Any, KEY : Serializable> : JpaSpecificationExecutor<ENTITY>, JpaRepository<ENTITY, KEY> {


    @JvmDefault
    fun findAllBy(specification: KSpecification<ENTITY>): List<ENTITY> {
        val jpaSpecification = toJpaSpecification(specification)
        return findAll(jpaSpecification)
    }

    @JvmDefault
    fun findAllBy(sort: Sort, specification: KSpecification<ENTITY>): List<ENTITY> {
        val jpaSpecification = toJpaSpecification(specification)
        return findAll(jpaSpecification, sort)
    }

    @JvmDefault
    fun findAllBy(pageable: Pageable, specification: KSpecification<ENTITY>): Page<ENTITY> {
        val jpaSpecification = toJpaSpecification(specification)
        return findAll(jpaSpecification, pageable)
    }

    @JvmDefault
    fun findOneBy(specificatoin: KSpecification<ENTITY>): ENTITY? {
        val jpaSpecification = toJpaSpecification(specificatoin)
        return findOne(jpaSpecification).orElse(null)
    }

    @JvmDefault
    fun countBy(specification: KSpecification<ENTITY>): Long {
        val jpaSpecification = toJpaSpecification(specification)
        return count(jpaSpecification)
    }


}

internal fun <ENTITY : Any> toJpaSpecification(query: KSpecification<ENTITY>): Specification<ENTITY> {
    val function: Function2<RootWrap<ENTITY, ENTITY>, RootWrap<ENTITY, ENTITY>, KSelect<ENTITY>> =
            { rootWrap: RootWrap<ENTITY, ENTITY>, _: RootWrap<ENTITY, ENTITY> ->
                query.invoke(rootWrap, rootWrap)
                rootWrap
            }

    return Specification<ENTITY> { root, criteriaQuery, criteriaBuilder ->
        val predicate = AnyDao.getPredicate(root, criteriaQuery as CriteriaQuery<*>, criteriaBuilder, function)
        predicate
    }
}

