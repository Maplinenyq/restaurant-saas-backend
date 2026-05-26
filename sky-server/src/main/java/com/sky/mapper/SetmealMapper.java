package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    // 分页查询
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    // 插入数据
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    //根据ID批量删除套餐
    void deleteByIds(List<Long> ids);

    //根据ID查询套餐数据
    @Select("select * from setmeal where id = #{id}")
    SetmealVO getById(Long id);

    //根据ID修改套餐数据
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    //动态查询套餐
    List<Setmeal> list(Setmeal setmeal);

    //根据套餐id查询菜品选项
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    //根据条件统计套餐数量
    Integer countByMap(Map map);

    //根据id查询套餐状态
    @Select("select status from setmeal where id = #{id}")
    Integer getStatusById(Long id);
}
