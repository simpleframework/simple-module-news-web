package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.permission.PermissionDept;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.news.INewsCategoryService;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.NewsStat;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.EElementEvent;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.ctx.CategoryBeanAwareHandler;
import net.simpleframework.mvc.component.ext.deptselect.DeptSelectBean;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.component.ui.propeditor.PropFields;
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

	@Override
	protected IDataQuery<?> categoryBeans(final ComponentParameter cp, final Object categoryId) {
		final NewsCategory category = _newsCategoryService.getBean(categoryId);
		return _newsCategoryService.queryChildren(category, NewsUtils.getDomainId(cp));
	}

	@Override
	public TreeNodes getCategoryTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode parent) {
		if (parent == null) {
			final TreeNodes nodes = TreeNodes.of();
			TreeNode tn = createRoot(treeBean, $m("NewsCategoryHandle.0"));
			tn.setAcceptdrop(true);
			setJsClickCallback(tn, null, null);
			final String imgBase = getImgBase(cp, NewsFormTPage.class);
			tn.setImage(imgBase + "news.png");
			nodes.add(tn);

			tn = new TreeNode(treeBean, parent, $m("NewsCategoryHandle.1"));
			setJsClickCallback(tn, null, EContentStatus.delete);
			tn.setImage(imgBase + "recycle_bin.png");
			tn.setContextMenu("none");
			final int nums = getNums(cp, null);
			if (nums > 0) {
				tn.setPostfixText("(" + nums + ")");
			}
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
				setJsClickCallback(parent, category, null);

				final ID domainId = NewsUtils.getDomainId(cp);
				if (domainId == null) {
					final PermissionDept dept = cp.getPermission().getDept(category.getDomainId());
					if (dept.getId() != null) {
						parent.setText("(" + SpanElement.color999(dept) + ") " + parent.getText());
					}
				}

				final int nums = getNums(cp, category);
				if (nums > 0) {
					parent.setPostfixText("(" + nums + ")");
				}
				parent.setImage(NewsUtils.getIconPath(cp, category));
			}
			return super.getCategoryTreenodes(cp, treeBean, parent);
		}
	}

	protected void setJsClickCallback(final TreeNode tn, final NewsCategory category,
			final EContentStatus status) {
		String params = "categoryId=";
		if (category != null) {
			params += category.getId();
		}
		params += "&status=";
		if (status != null) {
			params += status.name();
		}
		tn.setJsClickCallback(CategoryTableLCTemplatePage.createTableRefresh(params).toString());
	}

	private int getNums(final PageParameter pp, final NewsCategory category) {
		final ID domainId = NewsUtils.getDomainId(pp);
		if (category == null) {
			// 垃圾箱
			return _newsStatService.getAllNums_delete(domainId);
		} else {
			final ID categoryId = category.getId();
			if (domainId != null) {
				NewsStat stat = _newsStatService.getNewsStat(categoryId, null);
				int count = stat.getNums() - stat.getNums_delete();
				stat = _newsStatService.getNewsStat(categoryId, domainId);
				count += (stat.getNums() - stat.getNums_delete());
				return count;
			} else {
				return _newsStatService.getAllNums(categoryId, "nums")
						- _newsStatService.getAllNums(categoryId, "nums_delete");
			}
		}
	}

	@Override
	public TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode treeNode) {
		final Object o;
		if (treeNode != null && (o = treeNode.getDataObject()) instanceof NewsCategory) {
			treeNode.setImage(NewsUtils.getIconPath(cp, (NewsCategory) o));
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
			category.setDomainId(NewsUtils.getDomainId(cp));
		}

		if (cp.isLmanager()) {
			final String domain_id = cp.getParameter("domain_id");
			category.setDomainId(StringUtils.hasText(domain_id) ? ID.of(domain_id) : null);
		}
	}

	@Override
	public Map<String, Object> categoryEdit_attri(final ComponentParameter cp) {
		return ((KVMap) super.categoryEdit_attri(cp)).add(window_height, 380);
	}

	@Override
	protected AbstractComponentBean categoryEdit_createPropEditor(final ComponentParameter cp) {
		final NewsCategory category = _newsCategoryService
				.getBean(cp.getParameter(PARAM_CATEGORY_ID));
		final PropEditorBean propEditor = (PropEditorBean) super.categoryEdit_createPropEditor(cp);
		final PropFields fields = propEditor.getFormFields();
		if (cp.isLmanager()) {
			cp.addComponentBean("NewsCategoryHandle_deptSelect", DeptSelectBean.class).setOrg(true)
					.setBindingId("domain_id").setBindingText("domain_text");

			final InputComp domain_id = InputComp.hidden("domain_id");
			final InputComp domain_text = InputComp.textButton("domain_text")
					.setAttributes("readonly")
					.addEvent(EElementEvent.click, "$Actions['NewsCategoryHandle_deptSelect']();");
			PermissionDept org = null;
			if (category != null) {
				org = cp.getPermission().getDept(category.getDomainId());
			} else {
				org = AbstractMVCPage.getPermissionOrg(cp);
			}
			if (org != null) {
				domain_id.setDefaultValue(org.getId());
				domain_text.setDefaultValue(org.getText());
			}
			fields.add(2,
					new PropField($m("NewsCategoryHandle.2")).addComponents(domain_id, domain_text));
		}
		// propEditor.getFormFields().add(
		// 1,
		// new PropField($m("NewsCategoryHandle.1"))
		// .addComponents(InputComp.select("category_mark").setDefaultEnumValue(
		// ECategoryMark.normal, ECategoryMark.builtIn)));
		return propEditor;
	}
}
