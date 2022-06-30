package com.ithealth.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ithealth.dao.CheckItemDao;
import com.ithealth.entity.PageResult;
import com.ithealth.entity.QueryPageBean;
import com.ithealth.pojo.CheckItem;
import com.ithealth.service.CheckItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 检查项服务
 */
@Service(interfaceClass = CheckItemService.class)   //加了事务注解@Transactional后，必须明确当前服务实现的是哪个服务接口
@Transactional
public class CheckItemServiceImpl implements CheckItemService {

    @Autowired
    private CheckItemDao checkItemDao;

    //新增检查项
    @Override
    public void add(CheckItem checkItem) {
        checkItemDao.add(checkItem);
    }

    //分页查询检查项
    @Override
    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer currentPage = queryPageBean.getCurrentPage();
        Integer pageSize = queryPageBean.getPageSize();
        String queryString = queryPageBean.getQueryString();
        //完成分页查询，基于mybatis框架提供的分页助手来完成
        //在数据库中的分页查询select * from t_checkitem limit 0，10  //0表示第一页,10条数据
        //mybatis框架提供的分页助手select * from t_checkitem; 不需要写(limit 0，10),交由分页助手来完成
        PageHelper.startPage(currentPage,pageSize);
        Page<CheckItem> page = checkItemDao.selectByCondition(queryString);
        long total = page.getTotal();
        List<CheckItem> rows = page.getResult();
        return new PageResult(total,rows);
    }

    //根据id删除检查项
    @Override
    public void deleteById(Integer id) throws RuntimeException{
        //判断当前检查项是否已经关联到了检查组中
        long count = checkItemDao.findCountByCheckItemId(id);
        if (count>0){
            //当前检查项已经被关联到检查组，不允许删除
            throw new RuntimeException("当前检查项目被引用，不能删除");
        }else {
            //删除检查项数据
            checkItemDao.deleteById(id);
        }
    }

    //编辑检查项
    @Override
    public void edit(CheckItem checkItem) {
        checkItemDao.edit(checkItem);
    }

    @Override
    public CheckItem findById(Integer id) {
        return checkItemDao.findById(id);
    }
}
