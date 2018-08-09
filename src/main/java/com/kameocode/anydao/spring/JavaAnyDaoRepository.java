package com.kameocode.anydao.spring;

import com.kameocode.anydao.AnyDao;
import com.kameocode.anydao.KSelect;
import com.kameocode.anydao.wraps.RootWrap;
import kotlin.jvm.functions.Function2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@NoRepositoryBean
public interface JavaAnyDaoRepository<ENTITY, KEY extends Serializable> extends JpaSpecificationExecutor<ENTITY>, JpaRepository<ENTITY, KEY> {

    default List<ENTITY> findAllBy(Consumer<RootWrap<ENTITY, ENTITY>> query) {
        Specification<ENTITY> specification = toSpecification(query);
        return findAll(specification);
    }

    default List<ENTITY> findAllBy(Consumer<RootWrap<ENTITY, ENTITY>> query, Sort sort) {
        Specification<ENTITY> specification = toSpecification(query);
        return findAll(specification, sort);
    }

    default Page<ENTITY> findAllBy(Consumer<RootWrap<ENTITY, ENTITY>> query, Pageable pageable) {
        Specification<ENTITY> specification = toSpecification(query);
        return findAll(specification, pageable);
    }

    default Optional<ENTITY> findOneBy(Consumer<RootWrap<ENTITY, ENTITY>> query) {
        Specification<ENTITY> specification = toSpecification(query);
        return findOne(specification);
    }

    default long countBy(Consumer<RootWrap<ENTITY, ENTITY>> query) {
        Specification<ENTITY> specification = toSpecification(query);
        return count(specification);
    }

    @NotNull
    default Specification<ENTITY> toSpecification(Consumer<RootWrap<ENTITY, ENTITY>> query) {
        Function2 function = (Function2<RootWrap<ENTITY, ENTITY>, RootWrap<ENTITY, ENTITY>, KSelect<ENTITY>>) (rootWrap, rootWrap2) -> {
            query.accept(rootWrap);
            return rootWrap;
        };
        return (root, criteriaQuery, criteriaBuilder) ->
                AnyDao.getPredicate(root, criteriaQuery, criteriaBuilder, function);
    }

}


