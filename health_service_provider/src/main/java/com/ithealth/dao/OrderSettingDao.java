package com.ithealth.dao;

import com.ithealth.pojo.OrderSetting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface OrderSettingDao {

    public void add(OrderSetting list);
    public void editNumberByOrderDate(OrderSetting orderSetting);
    public long findCountByOrderDate(Date orderDate);

    List<OrderSetting> getOrderSettingByMonth(HashMap<String, String> map);
}
