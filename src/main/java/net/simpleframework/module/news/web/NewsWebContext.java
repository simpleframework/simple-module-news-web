package net.simpleframework.module.news.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.Module;
import net.simpleframework.ctx.ModuleFunctions;
import net.simpleframework.ctx.ModuleRefUtils;
import net.simpleframework.module.news.impl.NewsContext;
import net.simpleframework.module.news.web.page.t1.NewsMgrPage;
import net.simpleframework.mvc.ctx.WebModuleFunction;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsWebContext extends NewsContext implements INewsWebContext {
	@Override
	public String getAttachmentMaxSize() {
		return null;
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
		return ModuleRefUtils.getRef("net.simpleframework.module.news.web.NewsFavoriteRef");
	}

	@Override
	public IModuleRef getLogRef() {
		return ModuleRefUtils.getRef("net.simpleframework.module.news.web.NewsLogRef");
	}

	@Override
	public IModuleRef getTagRef() {
		return ModuleRefUtils.getRef("net.simpleframework.module.news.web.NewsTagRef");
	}

	@Override
	public IModuleRef getVoteRef() {
		return ModuleRefUtils.getRef("net.simpleframework.module.news.web.NewsVoteRef");
	}

	@Override
	protected Module createModule() {
		return super.createModule().setDefaultFunction(MODULE_NAME + "-NewsMgrPage");
	}

	@Override
	protected ModuleFunctions getFunctions() {
		return ModuleFunctions.of((WebModuleFunction) new WebModuleFunction(this, NewsMgrPage.class)
				.setName(MODULE_NAME + "-NewsMgrPage").setText($m("NewsContext.0")));
	}
}
