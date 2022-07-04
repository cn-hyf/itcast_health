package com.ithealth.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ithealth.constant.MessageConstant;
import com.ithealth.constant.RedisConstant;
import com.ithealth.entity.PageResult;
import com.ithealth.entity.QueryPageBean;
import com.ithealth.entity.Result;
import com.ithealth.pojo.Setmeal;
import com.ithealth.service.SetmealService;
import com.ithealth.utils.QiniuUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.UUID;

/**
 * 体检套餐管理
 */
@RestController
@RequestMapping("/setmeal")
public class SetMealController {

    @Reference
    private SetmealService setmealService;

    //使用JedisPool操作redis服务
    @Autowired
    private JedisPool jedisPool;

    //文件上传,路径就是页面中写的action="/setmeal/upload.do"    element中规定：action必选参数：上传的地址。name="imgFile"：name是上传的文件字段名
    // 然后还需要在springmvc.xml文件中配置上传文件的配置，它是springMVC提供的上传文件的功能
    @RequestMapping("/upload")
    public Result upload(@RequestParam("imgFile") MultipartFile imgFile) {
        String originalFilename = imgFile.getOriginalFilename();//原始文件名     如这种文件名：e174ff3de78c366d7512e83cd8f5ebed.jpg
        //截取后缀名jpg
        int index = originalFilename.lastIndexOf(".");//获取最后一个符号.的索引
        String extention = originalFilename.substring(index); //截取最后一个符号.后面的全部字符，这里截取的是jpg

        String fileName = UUID.randomUUID().toString() + extention; //文件名组成  uuid+图片的后缀

        try {
            //将文件上传到七牛云
            QiniuUtils.upload2Qiniu(imgFile.getBytes(), fileName);
            //将图片上传图片存入redis，基于Redis的set集合存储
            jedisPool.getResource().sadd(RedisConstant.SETMEAL_PIC_RESOURCES, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.PIC_UPLOAD_FAIL);
        }
        return new Result(true, MessageConstant.PIC_UPLOAD_SUCCESS, fileName);    //上传成功后，返回给前端的data中只有文件名。前端通过response.data接收
    }

    //新增套餐，同时需要关联检查组
    @RequestMapping("/add")
    public Result add(@RequestBody Setmeal setmeal, Integer[] checkgroupIds) {
        try {
            setmealService.add(setmeal, checkgroupIds);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.ADD_SETMEAL_FAIL);
        }
        return new Result(true, MessageConstant.ADD_SETMEAL_SUCCESS);
    }

    //条件分页查询
    @RequestMapping("/findPage")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        return setmealService.pageQuery(queryPageBean);
    }

    //根据id查询一条套餐组信息
    @RequestMapping("/findById")
    public Result findById(Integer id) {
        try {
            Setmeal setmeal = setmealService.findById(id);
            return new Result(true, MessageConstant.QUERY_SETMEAL_SUCCESS, setmeal);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_SETMEAL_FAIL);
        }
    }

    //如果是public List<Integer>这样写的话，前端获取后端传过来的数据为res.data
    //如果是public Result findById(Integer id)这样写的话，前端获取后端传过来的数据为res.data.data
    //根据套餐组id查询检查组包含的多个检查项id,返回给前端的是多个id
    @RequestMapping("/findCheckGroupIdsBySetmealId")
    public Result findCheckGroupIdsBySetmealId(Integer id) {
        try {
            List<Integer> checkGroupIds = setmealService.findCheckGroupIdsBySetmealId(id);
            return new Result(true, MessageConstant.QUERY_SETMEAL_SUCCESS, checkGroupIds);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_SETMEAL_FAIL);
        }
    }

    //编辑套餐组信息
    @RequestMapping("/edit")
    public Result edit(@RequestBody Setmeal setmeal,Integer[] checkgroupIds){
        try {
            setmealService.edit(setmeal,checkgroupIds);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,MessageConstant.EDIT_SETMEAL_FAIL);
        }
        return new Result(true,MessageConstant.EDIT_SETMEAL_SUCCESS);
    }

    //根据id删除数据
    @RequestMapping("/delete")
    public Result delete(Integer id){
        try {
            setmealService.deleteById(id);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,MessageConstant.DELETE_SETMEAL_FAIL);
        }
        return new Result(true,MessageConstant.DELETE_SETMEAL_SUCCESS);
    }
}
