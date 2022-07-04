package com.ithealth.dao;

import com.github.pagehelper.Page;
import com.ithealth.pojo.CheckGroup;

import java.util.List;
import java.util.Map;

public interface CheckGroupDao {
    public void add(CheckGroup checkGroup);
    public void setCheckGroupAndCheckItem(Map map);
    Page<CheckGroup> findByCondition(String queryString);
    public CheckGroup findById(Integer id);
    public List<Integer> findCheckItemIdsByCheckGroupId(Integer id);
    void edit(CheckGroup checkGroup);
    void deleteAssocication(Integer id);
    public void deleteById(Integer id);
    List<CheckGroup> findAll();
}
