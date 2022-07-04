package com.ithealth.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ithealth.dao.CheckGroupDao;
import com.ithealth.entity.PageResult;
import com.ithealth.entity.QueryPageBean;
import com.ithealth.pojo.CheckGroup;
import com.ithealth.service.CheckGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service(interfaceClass = CheckGroupService.class)
@Transactional
public class CheckGroupServiceImpl implements CheckGroupService {

    @Autowired
    private CheckGroupDao checkGroupDao;



    //新增检查组，同时需要让检查组关联检查项
    @Override
    public void add(CheckGroup checkGroup, Integer[] checkitemIds) {
        //新增检查组，操作t_checkgroup表
        checkGroupDao.add(checkGroup);
        Integer checkGroupId = checkGroup.getId();// checkGroupDao.add(checkGroup)新添加了一条数据，我们通过mybatis框架提供的selectKey标签来获得这条新增数据的id(自增产生的id值)

        //设置检查组和检查项的多对多的关联关系，操作t_checkgroup_checkitem表
        if (checkitemIds != null && checkitemIds.length>0){
            for (Integer checkItemId:checkitemIds){
                HashMap<String, Integer> map = new HashMap<>();
                map.put("checkgroup_id",checkGroupId);
                map.put("checkitem_id",checkItemId);
                checkGroupDao.setCheckGroupAndCheckItem(map);
            }
        }
    }

    //分页查询
    @Override
    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer currentPage = queryPageBean.getCurrentPage();
        Integer pageSize = queryPageBean.getPageSize();
        String queryString = queryPageBean.getQueryString();
        PageHelper.startPage(currentPage,pageSize); //这是mybatis的分页助手，返回值是一个Page对象
        Page<CheckGroup> page = checkGroupDao.findByCondition(queryString);
        return new PageResult(page.getTotal(),page.getResult());
    }

    //根据id查询检查组信息
    @Override
    public CheckGroup findById(Integer id) {
        return checkGroupDao.findById(id);
    }

    //根据检查组id查询多条检查项的id
    @Override
    public List<Integer> findCheckItemIdsByCheckGroupId(Integer id) {
        return checkGroupDao.findCheckItemIdsByCheckGroupId(id);
    }

    //编辑检查组信息，同时需要关联检查项（可能会情况：1.出现检查组关联新的检查项。2.取消几条以前已经关联的检查项，并重新勾选了几条新的检查项）
    //解决方式(全部删除后在更新)，在中间表删除检查组id,就是删除了所有的已经关联的检查项id。
    @Override
    public void edit(CheckGroup checkGroup, Integer[] checkitemIds) {
        //修改检查组基本信息，操作检查组t_checkgroup表
        checkGroupDao.edit(checkGroup);
        //清理当前检查组关联的检查项，操作中间关系表t_checkgroup_checkitem表
        checkGroupDao.deleteAssocication(checkGroup.getId());
        //重新建立当前检查组和检查项的关联关系
        Integer checkGroupId = checkGroup.getId();
        if (checkitemIds!=null && checkitemIds.length>0){
            for (Integer checkitemId : checkitemIds){
                HashMap<String, Integer> map = new HashMap<>();
                map.put("checkgroup_id",checkGroupId);
                map.put("checkitem_id",checkitemId);
                checkGroupDao.setCheckGroupAndCheckItem(map);
            }
        }
    }

    //根据id删除检查组，检查组中可能含有多条检查项信息，需要先删除中间表，再删除检查组
    @Override
    public void deleteById(Integer id) {
        //清理当前检查组关联的检查项，操作中间关系表t_checkgroup_checkitem表
        checkGroupDao.deleteAssocication(id);
        //根据id删除当前检查组
        checkGroupDao.deleteById(id);
    }

    //查询检查组的所有数据，不分页
    @Override
    public List<CheckGroup> findAll() {
        return checkGroupDao.findAll();
    }
}
