package com.nyq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nyq.constant.MessageConstant;
import com.nyq.constant.StatusConstant;
import com.nyq.dto.SetmealDTO;
import com.nyq.dto.SetmealPageQueryDTO;
import com.nyq.entity.Dish;
import com.nyq.entity.Setmeal;
import com.nyq.entity.SetmealDish;
import com.nyq.exception.DeletionNotAllowedException;
import com.nyq.exception.SetmealEnableFailedException;
import com.nyq.mapper.DishMapper;
import com.nyq.mapper.SetmealDishMapper;
import com.nyq.mapper.SetmealMapper;
import com.nyq.result.PageResult;
import com.nyq.service.SetmealService;
import com.nyq.vo.DishItemVO;
import com.nyq.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    // 分页查询
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //新增套餐
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(StatusConstant.ENABLE);
        setmealMapper.insert(setmeal);

        //新增菜品
        Long setmealId = setmeal.getId();
        //向套餐插入菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            //遍历插入套餐ID
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    //根据ID批量删除套餐
    @Override
    public void deleteByIds(List<Long> ids) {
        //启售的套餐不能删除
        for (Long id : ids) {
            Integer setmealStatus = setmealMapper.getStatusById(id);
            if(Objects.equals(setmealStatus, StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //先删除套餐和菜品的关联数据
        setmealDishMapper.deleteBySetmealIds(ids);
        //再删除套餐
        setmealMapper.deleteByIds(ids);
    }

    //根据ID查询套餐
    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setmealMapper.getById(id);
        setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        //启售的套餐不能删除
        if(Objects.equals(setmealVO.getStatus(), StatusConstant.ENABLE)){
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        return setmealVO;
    }

    //根据ID修改套餐状态
    @Override
    public void startOrStop(Integer status, Long id) {
        //查询套餐内有无未启售的菜品，否则提示套餐内有未启售的菜品无法启售
        if(Objects.equals(status, StatusConstant.ENABLE)){
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            for (SetmealDish setmealDish : setmealDishes) {
                Dish dish = dishMapper.getById(setmealDish.getDishId());
                if(dish != null && Objects.equals(dish.getStatus(), StatusConstant.DISABLE)){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    //根据ID修改套餐
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        //删除菜品
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //向套餐插入菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            //遍历插入套餐ID
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealDTO.getId());
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    //条件查询
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    //根据套餐ID查询包含的菜品列表
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}

