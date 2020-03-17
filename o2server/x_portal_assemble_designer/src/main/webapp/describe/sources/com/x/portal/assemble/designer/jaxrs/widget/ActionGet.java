package com.x.portal.assemble.designer.jaxrs.widget;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.cache.ApplicationCache;
import com.x.base.core.project.exception.ExceptionAccessDenied;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.portal.assemble.designer.Business;
import com.x.portal.core.entity.Portal;
import com.x.portal.core.entity.Widget;

import net.sf.ehcache.Element;

class ActionGet extends BaseAction {
	ActionResult<Wo> execute(EffectivePerson effectivePerson, String id) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			Business business = new Business(emc);
			ActionResult<Wo> result = new ActionResult<>();
			Wo wo = null;
			String cacheKey = ApplicationCache.concreteCacheKey(id);
			Element element = cache.get(cacheKey);
			if ((null != element) && (null != element.getObjectValue())) {
				wo = (Wo) element.getObjectValue();
			} else {
				Widget widget = emc.find(id, Widget.class);
				if (null == widget) {
					throw new ExceptionEntityNotExist(id, Widget.class);
				}
				wo = Wo.copier.copy(widget);
				cache.put(new Element(cacheKey, wo));
			}
			Portal portal = emc.find(wo.getPortal(), Portal.class);
			if (null == portal) {
				throw new ExceptionEntityNotExist(id, Portal.class);
			}
			if (!business.editable(effectivePerson, portal)) {
				throw new ExceptionAccessDenied(effectivePerson.getDistinguishedName());
			}
			result.setData(wo);
			return result;
		}
	}

	public static class Wo extends Widget {

		private static final long serialVersionUID = 6147694053942736622L;

		static WrapCopier<Widget, Wo> copier = WrapCopierFactory.wo(Widget.class, Wo.class, null,
				JpaObject.FieldsInvisible);
	}

}