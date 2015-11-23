package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.common.ID;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsCategoryService;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.NewsStat;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.ctx.CategoryBeanAwareHandler;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
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
		return _newsCategoryService;
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
			final String imgBase = getImgBase(cp, NewsFormTPage.class);
			tn.setImage(imgBase + "news.png");
			nodes.add(tn);

			tn = new TreeNode(treeBean, parent, $m("NewsCategoryHandle.1"));
			tn.setJsClickCallback(CategoryTableLCTemplatePage.createTableRefresh(
					"categoryId=&status=" + EContentStatus.delete.name()).toString());
			tn.setImage(imgBase + "recycle_bin.png");
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
				final String imgBase = getImgBase(cp, NewsFormTPage.class);
				setCount(parent, getNums(cp, category));
				parent.setImage(imgBase + "folder.png");
			}
			return super.getCategoryTreenodes(cp, treeBean, parent);
		}
	}

	private int getNums(final PageParameter pp, final NewsCategory category) {
		final ID categoryId = category.getId();
		NewsStat stat = _newsStatService.getNewsStat(categoryId, null);
		int c = stat.getNums() - stat.getNums_delete();
		final ID domainId = pp.getLDomainId();
		if (domainId != null) {
			stat = _newsStatService.getNewsStat(categoryId, domainId);
			c += (stat.getNums() - stat.getNums_delete());
		}
		return c;
	}

	@Override
	public TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode treeNode) {
		if (treeNode != null) {
			treeNode.setImage(getImgBase(cp, NewsFormTPage.class) + "folder.png");
			final Object o = treeNode.getDataObject();
			if (o instanceof NewsCategory) {
				setCount(treeNode, getNums(cp, (NewsCategory) o));
			}
		}
		return super.getCategoryTreenodes(cp, treeBean, treeNode);
	}

	@Override
	protected void onLoaded_dataBinding(final ComponentParameter cp,
			final Map<String, Object> dataBinding, final PageSelector selector,
			final NewsCategory category) {
	}

	@Override
	protected void onSave_setProperties(final ComponentParameter cp, final NewsCategory category,
			final boolean insert) {
		if (insert) {
			category.setUserId(cp.getLoginId());
		}
	}

	@Override
	public Map<String, Object> categoryEdit_attri(final ComponentParameter cp) {
		return ((KVMap) super.categoryEdit_attri(cp)).add(window_height, 380);
	}

	@Override
	protected AbstractComponentBean categoryEdit_createPropEditor(final ComponentParameter cp) {
		final PropEditorBean propEditor = (PropEditorBean) super.categoryEdit_createPropEditor(cp);

		// propEditor.getFormFields().add(
		// 1,
		// new PropField($m("NewsCategoryHandle.1"))
		// .addComponents(InputComp.select("category_mark").setDefaultEnumValue(
		// ECategoryMark.normal, ECategoryMark.builtIn)));
		return propEditor;
	}
}
