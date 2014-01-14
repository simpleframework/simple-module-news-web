package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.common.content.ECategoryMark;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsCategoryService;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.page.NewsForm;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.ctx.CategoryBeanAwareHandler;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsCategoryHandle extends CategoryBeanAwareHandler<NewsCategory> implements
		INewsContextAware {

	@Override
	protected INewsCategoryService getBeanService() {
		return context.getNewsCategoryService();
	}

	@Override
	protected IDataQuery<?> categoryBeans(final ComponentParameter cp, final Object categoryId) {
		final INewsCategoryService service = getBeanService();
		return service.queryChildren(service.getBean(categoryId));
	}

	private void setCount(final TreeNode tn, final int cc) {
		if (cc > 0) {
			tn.setPostfixText("(" + cc + ")");
		}
	}

	@Override
	public TreeNodes getCategoryTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent) {
		if (parent == null) {
			final TreeNodes nodes = TreeNodes.of();
			TreeNode tn = createRoot(treeBean, $m("NewsCategoryHandle.0"));
			tn.setAcceptdrop(true);
			tn.setJsClickCallback(CategoryTableLCTemplatePage
					.createTableRefresh("categoryId=&status=").toString());
			final String imgBase = getImgBase(cp, NewsForm.class);
			tn.setImage(imgBase + "news.png");
			nodes.add(tn);

			tn = new TreeNode(treeBean, parent, $m("NewsCategoryHandle.2"));
			tn.setJsClickCallback(CategoryTableLCTemplatePage.createTableRefresh(
					"categoryId=&status=" + EContentStatus.delete.name()).toString());
			tn.setImage(imgBase + "recycle_bin.png");
			setCount(tn, context.getNewsService().queryBeans(null, EContentStatus.delete).getCount());
			tn.setContextMenu("none");
			nodes.add(tn);
			return nodes;
		} else {
			String img;
			if ((img = parent.getImage()) != null && img.endsWith("recycle_bin.png")) {
				return null;
			}
			final Object o = parent.getDataObject();
			if (o instanceof NewsCategory) {
				final NewsCategory category = (NewsCategory) o;
				parent.setJsClickCallback(CategoryTableLCTemplatePage.createTableRefresh(
						"status=&categoryId=" + category.getId()).toString());
				final String imgBase = getImgBase(cp, NewsForm.class);
				setCount(parent, context.getNewsService().count(category));
				parent.setImage(imgBase + "folder.png");
			}
			return super.getCategoryTreenodes(cp, treeBean, parent);
		}
	}

	@Override
	public TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode treeNode) {
		if (treeNode != null) {
			treeNode.setImage(getImgBase(cp, NewsForm.class) + "folder.png");
			final Object o = treeNode.getDataObject();
			if (o instanceof NewsCategory) {
				setCount(treeNode, context.getNewsService().count((NewsCategory) o));
			}
		}
		return super.getCategoryTreenodes(cp, treeBean, treeNode);
	}

	@Override
	protected void onLoaded_dataBinding(final ComponentParameter cp,
			final Map<String, Object> dataBinding, final PageSelector selector,
			final NewsCategory category) {
		if (category != null) {
			dataBinding.put("category_mark", category.getMark());
		}
	}

	@Override
	protected void onSave_setProperties(final ComponentParameter cp, final NewsCategory category,
			final boolean insert) {
		if (insert) {
			category.setUserId(cp.getLoginId());
		}
		category.setMark(Convert.toEnum(ECategoryMark.class, cp.getParameter("category_mark")));
	}

	@Override
	public Map<String, Object> categoryEdit_attri(final ComponentParameter cp) {
		return ((KVMap) super.categoryEdit_attri(cp)).add(window_height, 330);
	}

	@Override
	protected AbstractComponentBean categoryEdit_createPropEditor(final ComponentParameter cp) {
		final PropEditorBean propEditor = (PropEditorBean) super.categoryEdit_createPropEditor(cp);

		propEditor.getFormFields().add(
				1,
				new PropField($m("NewsCategoryHandle.1")).addComponents(InputComp.select(
						"category_mark").setDefaultValue(ECategoryMark.normal, ECategoryMark.builtIn)));
		return propEditor;
	}
}
