package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    //分页查询
    Page<Category> query(CategoryPageQueryDTO categoryPageQueryDTO);

    //新增分类
    @Insert("insert into category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    //根据Id删除
    @Delete("delete from category where id = #{id}")
    void deleteByIds(long id);

    //根据ID编辑分类
    void update(Category category);

    //根据类型查询分类
    List<Category> queryByType(Integer type);
}
