package com.ithealth.service;

import com.ithealth.entity.PageResult;
import com.ithealth.entity.QueryPageBean;
import com.ithealth.pojo.CheckItem;

public interface CheckItemService {
    public void add(CheckItem checkItem);

    PageResult pageQuery(QueryPageBean queryPageBean);

    void deleteById(Integer id);

    void edit(CheckItem checkItem);

    CheckItem findById(Integer id);
}
