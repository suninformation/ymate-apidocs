/*
 * Copyright 2007-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.apidocs;

import net.ymate.apidocs.core.base.ActionInfo;
import net.ymate.apidocs.core.base.ApiInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/5/15 上午10:15
 * @version 1.0
 */
public class NavItem implements Serializable {

    public static List<NavItem> create(List<ApiInfo> apis) {
        List<NavItem> _items = new ArrayList<NavItem>();
        for (ApiInfo _apiInfo : apis) {
            NavItem _item = new NavItem(_apiInfo.getName(), _apiInfo.getLinkUrl());
            for (ActionInfo _action : _apiInfo.getActions()) {
                _item.addSubItem(new NavItem(_action.getDispName(), _action.getLinkUrl()));
            }
            _items.add(_item);
        }
        return _items;
    }

    private String title;

    private String url;

    private List<NavItem> subItems = new ArrayList<NavItem>();

    public NavItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public List<NavItem> getSubItems() {
        return subItems;
    }

    public NavItem addSubItem(NavItem item) {
        if (item != null) {
            this.subItems.add(item);
        }
        return this;
    }

    public NavItem addSubItems(List<NavItem> items) {
        if (items != null && !items.isEmpty()) {
            this.subItems.addAll(items);
        }
        return this;
    }
}
