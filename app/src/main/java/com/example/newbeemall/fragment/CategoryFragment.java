package com.example.newbeemall.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.newbeemall.R;
import com.example.newbeemall.SearchActivity;
import com.example.newbeemall.adapter.CategoryTagAdapter;
import com.example.newbeemall.model.CategoryItem;
import com.example.newbeemall.util.HttpUtil;
import com.example.newbeemall.util.JsonUtil;
import com.example.newbeemall.widget.ExpandedGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类页 - 左侧分类列表（选中高亮），右侧子分类入口
 */
public class CategoryFragment extends Fragment {
    private final List<CategoryItem> categories = new ArrayList<>();
    private final List<CategoryItem> currentChildren = new ArrayList<>();
    private ListView lvCategory;
    private int selectedPosition = 0;
    private CategoryTagAdapter childAdapter;
    private TextView tvSelectedCategoryTitle;

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
        lvCategory = view.findViewById(R.id.lvCategory);
        ExpandedGridView gvCategoryGoods = view.findViewById(R.id.gvCategoryGoods);
        tvSelectedCategoryTitle = view.findViewById(R.id.tvSelectedCategoryTitle);
        TextView tvCategorySearch = view.findViewById(R.id.tvCategorySearch);
        tvCategorySearch.setOnClickListener(v -> startActivity(new Intent(requireContext(), SearchActivity.class)));

        // 自定义适配器，带选中高亮
        ArrayAdapter<CategoryItem> categoryAdapter = new ArrayAdapter<CategoryItem>(requireContext(),
                R.layout.item_category_left, R.id.tvCategoryName, categories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = v.findViewById(R.id.tvCategoryName);
                if (position == selectedPosition) {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                    tv.getPaint().setFakeBoldText(true);
                } else {
                    tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                    tv.getPaint().setFakeBoldText(false);
                }
                return v;
            }
        };

        lvCategory.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvCategory.setAdapter(categoryAdapter);
        childAdapter = new CategoryTagAdapter(requireContext(), currentChildren);
        gvCategoryGoods.setAdapter(childAdapter);

        lvCategory.setOnItemClickListener((parent, itemView, position, id) -> {
            selectedPosition = position;
            categoryAdapter.notifyDataSetChanged();
            selectCategory(position);
        });
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
                    ((ArrayAdapter) lvCategory.getAdapter()).notifyDataSetChanged();
                    if (!categories.isEmpty()) {
                        selectedPosition = 0;
                        selectCategory(0);
                        ((ArrayAdapter) lvCategory.getAdapter()).notifyDataSetChanged();
                    }
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
        tvSelectedCategoryTitle.setText(selected.getName());
        childAdapter.notifyDataSetChanged();
    }

    private void openCategorySearch(CategoryItem item) {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        intent.putExtra("goodsCategoryId", item.getId());
        intent.putExtra("title", item.getName());
        startActivity(intent);
    }
}
