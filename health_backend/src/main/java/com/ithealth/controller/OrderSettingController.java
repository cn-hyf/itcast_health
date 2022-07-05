package com.ithealth.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ithealth.constant.MessageConstant;
import com.ithealth.entity.Result;
import com.ithealth.pojo.OrderSetting;
import com.ithealth.service.OrderSettingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ithealth.utils.POIUtils.readExcel;

/**
 * 预约服务
 */
@RestController
@RequestMapping("/ordersetting")
public class OrderSettingController {

    @Reference
    private OrderSettingService orderSettingService;

    //文件上传，实现预约设置数据批量导入
    @RequestMapping("/upload")
    public Result upload(@RequestParam("excelFile") MultipartFile excelFile){
        try {
            //使用poi解析表格数据，类型是string类型，这是通用类型，需要变成我们需要的类型。
            List<String[]> list = readExcel(excelFile);//数组中只有两个字段
            List<OrderSetting> data = new ArrayList<>();
            for (String[] strings : list){
                String orderDate = strings[0];//获取字段1
                String number = strings[1];//获取字段2
                OrderSetting orderSetting = new OrderSetting(new Date(orderDate), Integer.parseInt(number));
                data.add(orderSetting);
            }
            //通过dubbo远程调用服务实现数据批量导入到数据库
            orderSettingService.add(data);
            return new Result(true,MessageConstant.IMPORT_ORDERSETTING_SUCCESS);
        }catch (IOException e){
            e.printStackTrace();
            //解析文件失败
            return new Result(false, MessageConstant.IMPORT_ORDERSETTING_FAIL);
        }
    }

    /**
     * 根据月份查询对应的预约设置数据
     * 需要返回给前端的数据格式为{ date: 1, number: 120, reservations: 1 },所以不能使用OrderSetting实体类对象，字段对应不上
     * @param date
     * @return
     */
    @RequestMapping("/getOrderSettingByMonth")
    public Result getOrderSettingByMonth(String date){//这个参数格式为：yyyy-MM
        try {
            List<Map> list = orderSettingService.getOrderSettingByMonth(date);
            return new Result(true,MessageConstant.GET_ORDERSETTING_SUCCESS,list);
        }catch (Exception e){
            return new Result(false,MessageConstant.GET_ORDERSETTING_FAIL);
        }
    }

    /**
     * 功能：根据日期修改可预约人数。修改单条数据
     * 前端传过来的参数是json数据，需要使用@RequestBody把它转化成java对象
     * number: "400"
     * orderDate: "2022-07-07"
     * 这两个参数刚好是实体类OrderSetting的字段。
     * 因为是修改操作，所以不需要返回值
     * @param orderSetting
     * @return
     */
    @RequestMapping("/editNumberByDate")
    public Result editNumberByDate(@RequestBody OrderSetting orderSetting){//这个参数格式为：yyyy-MM
        try {
            orderSettingService.editNumberByDate(orderSetting);

        }catch (Exception e){
            return new Result(false,MessageConstant.ORDERSETTING_FAIL);
        }
        return new Result(true,MessageConstant.ORDERSETTING_SUCCESS);
    }
}
