package com.ithealth.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ithealth.dao.OrderSettingDao;
import com.ithealth.pojo.OrderSetting;
import com.ithealth.service.OrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 预约设置服务
 */
@Service(interfaceClass = OrderSettingService.class)
@Transactional
public class OrderSettingServiceImpl implements OrderSettingService {

    @Autowired
    private OrderSettingDao orderSettingDao;

    //批量导入数据集合
    @Override
    public void add(List<OrderSetting> list) {
        if (list!=null&&list.size()>0){
            for (OrderSetting orderSetting:list){
                //判断当前日期是否已经进行了预约设置
                long countByOrderDate = orderSettingDao.findCountByOrderDate(orderSetting.getOrderDate());
                if (countByOrderDate>0){
                    //已经进行了预约设置，执行更新操作
                    orderSettingDao.editNumberByOrderDate(orderSetting);
                }else {
                    //没有进行预约设置，执行插入操作
                    orderSettingDao.add(orderSetting);
                }
            }
        }
    }

    //根据月份查询对应的预约设置数据
    @Override
    public List<Map> getOrderSettingByMonth(String date) {//这个参数格式为：yyyy-MM
        //数据库的字段orderDate格式是2022-07-05,使用between and来范围查询
        String begin = date + "-1"; //拼接后的数据为：2022-07-1
        String end = date + "-31";
        HashMap<String, String> map = new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);
        //根据日期范围查询预约设置数据
        List<OrderSetting> list = orderSettingDao.getOrderSettingByMonth(map); //dao返回值类型是List<OrderSetting>，而我们的方法需要返回的数据类型是List<Map>。需要转化。
        // 前端的数据格式为{ date: 1, number: 120, reservations: 1 }
        List<Map> result = new ArrayList<>();
        if (list!=null && list.size()>0){
            for (OrderSetting orderSetting:list){
                Map<String,Object> m = new HashMap<>();
                m.put("date",orderSetting.getOrderDate().getDate());//获取2022-07-05中的05
                m.put("number",orderSetting.getNumber());
                m.put("reservations",orderSetting.getReservations());
                result.add(m);
            }
        }
        return result; //result中的数据类型就是{ date: 1, number: 120, reservations: 1 }
    }


    /**
     * 根据日期修改可预约人数。修改单条数据
     * 如果以前有数据，那么就更新
     * 如果以前没有数据，那么就插入
     */
    @Override
    public void editNumberByDate(OrderSetting orderSetting) {

        //根据日期修改可预约人数
        long count = orderSettingDao.findCountByOrderDate(orderSetting.getOrderDate());
        if (count>0){
            //当前日期已经进行了预约设置，只需要更新即可
            orderSettingDao.editNumberByOrderDate(orderSetting);
        }else {
            //当前日期没有进行预约设置，需要插入
            orderSettingDao.add(orderSetting);
        }
    }
}
