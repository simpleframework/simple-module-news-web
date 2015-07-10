package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.template.AbstractTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsAdvPage extends AbstractTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addAjaxRequest(pp, "NewsAdvPage_reIndex").setConfirmMessage($m("NewsAdvPage.2"))
				.setHandlerMethod("doIndex");
	}

	@Override
	public Map<String, Object> createVariables(final PageParameter pp) {
		return ((KVMap) super.createVariables(pp)).add("LinkButton", LinkButton.class);
	}

	public IForward doIndex(final ComponentParameter cp) {
		newsContext.getNewsService().getLuceneService().rebuildIndex();
		return new JavascriptForward("alert('").append($m("NewsAdvPage.3")).append("');");
	}
}
