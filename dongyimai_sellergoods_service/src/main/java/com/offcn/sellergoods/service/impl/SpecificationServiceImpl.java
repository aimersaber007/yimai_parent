package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Specification;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;

    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(Specification specification) {
        //插入规格
        specificationMapper.insert(specification.getSpecification());


        if (null != specification.getSpecificationOptionList() && specification.getSpecificationOptionList().size() > 0) {
            //循环插入规格选项
            for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {

                //向规格选项中设置规格id
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                //保存
                tbSpecificationOptionMapper.insert(tbSpecificationOption);
            }
        }

    }

    /**
     * 增加
     */
	/*@Override
	public void add(TbSpecification specification) {
		specificationMapper.insert(specification);		
	}*/


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        //修改规格名称对象
        specificationMapper.updateByPrimaryKey(specification.getSpecification());

        TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
        criteria.andSpecIdEqualTo(specification.getSpecification().getId());
        //执行删除
        tbSpecificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
        //重新插入规格选项
        if (!CollectionUtils.isEmpty(specification.getSpecificationOptionList())) {
            for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
                //设置id
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                //再添加
                tbSpecificationOptionMapper.insert(tbSpecificationOption);
            }
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
	/*@Override
	public TbSpecification findOne(Long id){
		return specificationMapper.selectByPrimaryKey(id);
	}*/

    //根据ID获取实体
    public Specification findOne(Long id) {
        //查询id
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        //
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> tbSpecificationOptionList = tbSpecificationOptionMapper.selectByExample(example);
        //将规格名称和规格选项集合放入复合类型中
        Specification specification = new Specification();
        //将值放入复合集合中
        specification.setSpecification(tbSpecification);
        specification.setSpecificationOptionList(tbSpecificationOptionList);
        //返回复合类型
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            specificationMapper.deleteByPrimaryKey(id);
            //删除规格选项表中的数据
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(id);
            tbSpecificationOptionMapper.deleteByExample(example);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }
        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
