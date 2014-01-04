package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.DescriptionLocalUtils;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class StatusLogAddPage extends FormTableRowTemplatePage implements INewsContextAware {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.required, "#sl_description"));
	}

	@Override
	@Transaction(context = INewsContext.class)
	public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
		final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
		final INewsService service = context.getNewsService();
		final String[] arr = StringUtils.split(cp.getParameter("newsId"), ";");
		final String desc = cp.getParameter("sl_description");
		if (arr != null) {
			for (final String id : arr) {
				final News news = service.getBean(id);
				DescriptionLocalUtils.set(news, desc);
				news.setStatus(op);
				service.update(new String[] { "status" }, news);
			}
		}
		return super.onSave(cp).append(CategoryTableLCTemplatePage.createTableRefresh().toString());
	}

	@Override
	public String getTitle(final PageParameter pp) {
		final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
		return $m("StatusLogAddPage.1",
				op == EContentStatus.edit ? $m("NewsMgrPage.7") : op.toString());
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
		return TableRows.of(new TableRow(new RowField($m("StatusLogAddPage.0"), InputElement
				.textarea("sl_description")
				.setRows(6)
				.setText(
						$m("StatusLogAddPage.2",
								op == EContentStatus.edit ? $m("NewsMgrPage.7") : op.toString(),
								Convert.toDateString(new Date()), pp.getLogin())))));
	}
}
