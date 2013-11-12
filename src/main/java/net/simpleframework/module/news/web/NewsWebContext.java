package net.simpleframework.module.news.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.Module;
import net.simpleframework.module.news.impl.NewsContext;
import net.simpleframework.module.news.web.page.t1.NewsMgrPage;
import net.simpleframework.mvc.ctx.WebModuleFunction;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class NewsWebContext extends NewsContext implements INewsWebContext {

	@Override
	public void onInit(final IApplicationContext application) throws Exception {
		super.onInit(application);
	}

	@Override
	public NewsPageletCreator getPageletCreator() {
		return singleton(NewsPageletCreator.class);
	}

	@Override
	public NewsUrlsFactory getUrlsFactory() {
		return singleton(NewsUrlsFactory.class);
	}

	@Override
	public IModuleRef getFavoriteRef() {
		return getRef("net.simpleframework.module.news.web.NewsFavoriteRef");
	}

	@Override
	public IModuleRef getLogRef() {
		return getRef("net.simpleframework.module.news.web.NewsLogRef");
	}

	@Override
	public IModuleRef getTagRef() {
		return getRef("net.simpleframework.module.news.web.NewsTagRef");
	}

	@Override
	public IModuleRef getVoteRef() {
		return getRef("net.simpleframework.module.news.web.NewsVoteRef");
	}

	@Override
	public IModuleRef getPDFRef() {
		return getRef("net.simpleframework.module.news.web.NewsPDFRef");
	}

	@Override
	protected Module createModule() {
		return super.createModule().setDefaultFunction(
				new WebModuleFunction(NewsMgrPage.class).setName(MODULE_NAME + "-NewsMgrPage").setText(
						$m("NewsContext.0")));
	}
}
