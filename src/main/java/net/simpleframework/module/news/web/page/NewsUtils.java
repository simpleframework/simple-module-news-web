package net.simpleframework.module.news.web.page;

import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class NewsUtils implements INewsContextAware {

	public static NewsCategory getNewsCategory(final PageParameter pp) {
		return AbstractTemplatePage.getCacheBean(pp, _newsCategoryService, "categoryId");
	}
}
