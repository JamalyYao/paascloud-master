/*
 * Copyright (c) 2018. paascloud.net All Rights Reserved.
 * 项目名称：paascloud快速搭建企业级分布式微服务平台
 * 类名称：UacActionMainController.java
 * 创建人：刘兆明
 * 联系方式：paascloud.net@gmail.com
 * 开源地址: https://github.com/paascloud
 * 博客地址: http://blog.paascloud.net
 * 项目官网: http://paascloud.net
 */

package com.paascloud.provider.web;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Preconditions;
import com.paascloud.base.dto.LoginAuthDto;
import com.paascloud.core.support.BaseController;
import com.paascloud.core.utils.RequestUtil;
import com.paascloud.provider.model.domain.UacAction;
import com.paascloud.provider.model.dto.action.ActionMainQueryDto;
import com.paascloud.provider.model.dto.action.UacActionCheckCodeDto;
import com.paascloud.provider.model.dto.action.UacActionCheckUrlDto;
import com.paascloud.provider.model.dto.action.UacActionDto;
import com.paascloud.provider.model.dto.base.ModifyStatusDto;
import com.paascloud.provider.model.service.UacActionFeignApi;
import com.paascloud.provider.service.UacActionService;
import com.paascloud.wrapper.WrapMapper;
import com.paascloud.wrapper.Wrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;


/**
 * The class Uac action main controller.
 *
 * @author paascloud.net @gmail.com
 */
@RefreshScope
@RestController
@Api(value = "API - UacActionFeignClient", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UacActionFeignClient extends BaseController implements UacActionFeignApi {

	@Resource
	private UacActionService uacActionService;

	@Override
	public Wrapper queryUacActionListWithPage(ActionMainQueryDto action) {
		logger.info("查询角色列表actionQuery={}", action);
		PageInfo pageInfo = uacActionService.queryActionListWithPage(action);
		return WrapMapper.ok(pageInfo);
	}

	@Override
	public Wrapper deleteUacActionById(Long id) {
		int result = uacActionService.deleteActionById(id);
		return super.handleResult(result);
	}

	@Override
	public Wrapper batchDeleteByIdList(List<Long> deleteIdList) {
		logger.info("批量删除角色 idList={}", deleteIdList);
		uacActionService.batchDeleteByIdList(deleteIdList);
		return WrapMapper.ok();
	}

	@Override
	public Wrapper save(UacActionDto action) {
		LoginAuthDto loginAuthDto = RequestUtil.getLoginUser();
		UacAction uacAction = new UacAction();
		BeanUtils.copyProperties(action,uacAction);
		uacActionService.saveAction(uacAction, loginAuthDto);
		return WrapMapper.ok();
	}

	@Override
	public Wrapper modifyActionStatus(ModifyStatusDto modifyStatusDto) {
		logger.info("根据角色Id修改权限状态 modifyStatusDto={}", modifyStatusDto);
		Long actionId = modifyStatusDto.getId();
		Preconditions.checkArgument(actionId != null, "权限ID不能为空");

		UacAction uacRole = new UacAction();
		uacRole.setId(actionId);
		uacRole.setStatus(modifyStatusDto.getStatus());
		uacRole.setUpdateInfo(getLoginAuthDto());

		int result = uacActionService.update(uacRole);
		return super.handleResult(result);
	}

	@Override
	public Wrapper<Boolean> checkActionCode(UacActionCheckCodeDto uacActionCheckCodeDto) {
		logger.info("校验权限编码唯一性 uacActionCheckCodeDto={}", uacActionCheckCodeDto);

		Long id = uacActionCheckCodeDto.getActionId();
		String actionCode = uacActionCheckCodeDto.getActionCode();

		Example example = new Example(UacAction.class);
		Example.Criteria criteria = example.createCriteria();

		if (id != null) {
			criteria.andNotEqualTo("id", id);
		}
		criteria.andEqualTo("actionCode", actionCode);

		int result = uacActionService.selectCountByExample(example);
		return WrapMapper.ok(result < 1);
	}

	@Override
	public Wrapper<Boolean> checkActionUrl(UacActionCheckUrlDto uacActionCheckUrlDto) {
		logger.info("检测权限URL唯一性 uacActionCheckUrlDto={}", uacActionCheckUrlDto);

		Long id = uacActionCheckUrlDto.getActionId();
		String url = uacActionCheckUrlDto.getUrl();

		Example example = new Example(UacAction.class);
		Example.Criteria criteria = example.createCriteria();

		if (id != null) {
			criteria.andNotEqualTo("id", id);
		}
		criteria.andEqualTo("url", url);

		int result = uacActionService.selectCountByExample(example);
		return WrapMapper.ok(result < 1);
	}
}