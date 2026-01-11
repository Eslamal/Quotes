package com.example.qoutes.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.qoutes.databinding.ItemCategoryBinding
import com.example.qoutes.R


data class Category(
    val id: String,
    val name: String,
    val color: Int,
    val icon: Int
)

class CategoryAdapter(
    private val categories: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        with(holder.binding) {
            tvCategoryName.text = category.name
            ivCategoryIcon.setImageResource(category.icon)
            cardCategory.setCardBackgroundColor(root.context.getColor(category.color))

            root.setOnClickListener {
                onClick(category)
            }
        }
    }

    override fun getItemCount(): Int = categories.size
}