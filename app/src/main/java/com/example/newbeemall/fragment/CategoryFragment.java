package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newbeemall.R;
import com.example.newbeemall.SearchActivity;
import com.example.newbeemall.model.CategoryItem;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类页 - 左侧分类列表，右侧子分类入口
 */
public class CategoryFragment extends Fragment {
    private final List<CategoryItem> categories = new ArrayList<>();
    private final List<CategoryItem> currentChildren = new ArrayList<>();
    private ArrayAdapter<CategoryItem> categoryAdapter;
    private ArrayAdapter<CategoryItem> childAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lvCategory = view.findViewById(R.id.lvCategory);
        GridView gvCategoryGoods = view.findViewById(R.id.gvCategoryGoods);
        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_activated_1, categories);
        childAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, currentChildren);
        lvCategory.setAdapter(categoryAdapter);
        gvCategoryGoods.setAdapter(childAdapter);

        lvCategory.setOnItemClickListener((parent, itemView, position, id) -> selectCategory(position));
        gvCategoryGoods.setOnItemClickListener((parent, itemView, position, id) -> openCategorySearch(currentChildren.get(position)));
        loadCategories();
    }

    private void loadCategories() {
        new Thread(() -> {
            String result = HttpUtil.get("/api/v1/categories", requireContext());
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(requireContext(), "分类加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    categories.clear();
                    categories.addAll(JsonUtil.parseCategories(result));
                    categoryAdapter.notifyDataSetChanged();
                    if (!categories.isEmpty()) selectCategory(0);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "分类解析失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void selectCategory(int position) {
        if (position < 0 || position >= categories.size()) return;
        CategoryItem selected = categories.get(position);
        currentChildren.clear();
        if (selected.getChildren().isEmpty()) {
            currentChildren.add(selected);
        } else {
            currentChildren.addAll(selected.getChildren());
        }
        childAdapter.notifyDataSetChanged();
    }

    private void openCategorySearch(CategoryItem item) {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        intent.putExtra("goodsCategoryId", item.getId());
        intent.putExtra("title", item.getName());
        startActivity(intent);
    }
}
