package com.fuxin.springai.mapper;


import com.fuxin.springai.model.dbo.NovelDetail;
import org.springframework.data.repository.CrudRepository;

public interface NovelRepository extends CrudRepository<NovelDetail, String> {

    NovelDetail getByNovelId(String novelId);
}
