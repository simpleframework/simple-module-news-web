package net.simpleframework.module.news.web.page;

import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.AbstractComment;
import net.simpleframework.module.news.INewsCommentService;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.NewsComment;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.comments.ctx.CommentCtxHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsCommentHandler extends CommentCtxHandler<NewsComment> implements INewsContextAware {

	@Override
	public ID getOwnerId(final ComponentParameter cp) {
		return NewsUtils.getNews(cp).getId();
	}

	@Override
	public Map<String, Object> getFormParameters(final ComponentParameter cp) {
		final KVMap kv = (KVMap) super.getFormParameters(cp);
		kv.add("newsId", getOwnerId(cp));
		return kv;
	}

	@Override
	protected INewsCommentService getBeanService() {
		return _newsCommentService;
	}

	@Override
	public IDataQuery<?> comments(final ComponentParameter cp) {
		return getBeanService().queryComments(getOwnerId(cp));
	}

	@Transaction(context = INewsContext.class)
	@Override
	public JavascriptForward deleteComment(final ComponentParameter cp, final Object id) {
		return super.deleteComment(cp, id);
	}

	@Transaction(context = INewsContext.class)
	@Override
	public JavascriptForward addComment(final ComponentParameter cp) {
		final INewsCommentService service = getBeanService();
		final NewsComment comment = service.createBean();
		comment.setContentId(getOwnerId(cp));
		comment.setCreateDate(new Date());
		comment.setUserId(cp.getLoginId());
		comment.setCcomment(cp.getParameter(PARAM_COMMENT));
		final AbstractComment parent = service.getBean(cp.getParameter(PARAM_PARENTID));
		if (parent != null) {
			comment.setParentId(parent.getId());
		}
		service.insert(comment);
		return super.addComment(cp);
	}
}