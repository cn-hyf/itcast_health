package com.ithealth.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ithealth.constant.RedisConstant;
import com.ithealth.dao.SetmealDao;
import com.ithealth.entity.PageResult;
import com.ithealth.entity.QueryPageBean;
import com.ithealth.pojo.Setmeal;
import com.ithealth.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.List;

@Service(interfaceClass = SetmealService.class)
@Transactional
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealDao setmealDao;

    @Autowired
    private JedisPool jedisPool;

    //新增套餐，同时需要关联检查组
    @Override
    public void add(Setmeal setmeal, Integer[] checkgroupIds) {
        //新增套餐
        setmealDao.add(setmeal);
        Integer setmealId = setmeal.getId();//获取这条新增套餐数据的id
        //设置套餐和检查组多对多关系，操作t_setmeal_checkgroup
        if (checkgroupIds!=null && checkgroupIds.length>0){
            for (Integer checkgroupId:checkgroupIds){
                HashMap<String, Integer> map = new HashMap<>();
                map.put("setmealId",setmealId);
                map.put("checkgroupId",checkgroupId);
                setmealDao.setSetmealAndCheckGroup(map);
            }
        }
        //将图片上传图片存入redis，基于Redis的set集合存储
        String fileName = setmeal.getImg();
        jedisPool.getResource().sadd(RedisConstant.SETMEAL_PIC_DB_RESOURCES,fileName);
    }

    //条件分页查询
    @Override
    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer currentPage = queryPageBean.getCurrentPage();
        Integer pageSize = queryPageBean.getPageSize();
        String queryString = queryPageBean.getQueryString();
        PageHelper.startPage(currentPage,pageSize);
        Page<Setmeal> page  = setmealDao.findByCondition(queryString);
        return new PageResult(page.getTotal(),page.getResult());
    }

    //根据id查询一条套餐组信息
    @Override
    public Setmeal findById(Integer id) {
        return setmealDao.findById(id);
    }

    //根据套餐组id查询检查组包含的多个检查项id,返回给前端的是多个id
    @Override
    public List<Integer> findCheckGroupIdsBySetmealId(Integer id) {
        return setmealDao.findCheckGroupIdsBySetmealId(id);
    }

    //编辑套餐组信息
    @Override
    public void edit(Setmeal setmeal, Integer[] checkgroupIds) {
        //修改套餐组基本信息，操作检查组t_setmeal表
        setmealDao.edit(setmeal);
        //清理当前检查组关联的检查项，操作中间关系表t_setmeal_checkgroup表
        setmealDao.deleteAssocication(setmeal.getId());
        //重新建立当前检查组和检查项的关联关系
        Integer setmealId = setmeal.getId();
        if (checkgroupIds!=null && checkgroupIds.length>0){
            for (Integer checkgroupId:checkgroupIds){
                HashMap<String, Integer> map = new HashMap<>();
                map.put("setmeal_Id",setmealId);
                map.put("checkgroup_id",checkgroupId);
                setmealDao.setSetmealAndCheckGroup(map);
            }
        }
    }

    //根据id删除套餐组，套餐组中可能含有多条检查组信息，需要先删除中间表，再删除套餐组
    @Override
    public void deleteById(Integer id) {
        //清理当前套餐组关联的检查组项，操作中间关系表t_setmeal_checkgroup表
        setmealDao.deleteAssocication(id);
        //根据id删除当前套餐组
        setmealDao.deleteById(id);
    }

}
