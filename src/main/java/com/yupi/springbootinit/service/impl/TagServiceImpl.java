package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.Tag;
import com.yupi.springbootinit.service.TagService;
import com.yupi.springbootinit.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2024-01-23 16:51:46
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




