package com.ithealth.dao;

import com.github.pagehelper.Page;
import com.ithealth.pojo.Setmeal;

import java.util.List;
import java.util.Map;

public interface SetmealDao {

    public void add(Setmeal setmeal);
    public void setSetmealAndCheckGroup(Map map);

    Page<Setmeal> findByCondition(String queryString);

    Setmeal findById(Integer id);

    List<Integer> findCheckGroupIdsBySetmealId(Integer id);

    void edit(Setmeal setmeal);

    void deleteAssocication(Integer id);

    void deleteById(Integer id);
}
