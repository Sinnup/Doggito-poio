package com.fruse.dogedex.dogList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.fruse.dogedex.R
import com.fruse.dogedex.databinding.DogListItemBinding
import com.fruse.dogedex.core.model.Dog

class DogAdapter : ListAdapter<Dog, DogAdapter.DogViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Dog>() {
        override fun areItemsTheSame(oldItem: Dog, newItem: Dog): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Dog, newItem: Dog): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private var onItemClickListener: ((Dog) -> Unit)? = null

    fun setOnItemClickListener(onItemClickListener: (Dog) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }

    private var onLongItemClickListener: ((Dog) -> Unit)? = null

    fun setOnLongItemClickListener(onLongItemClickListener: (Dog) -> Unit) {
        this.onLongItemClickListener = onLongItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val binding = DogListItemBinding.inflate(LayoutInflater.from(parent.context))

        return DogViewHolder(binding)
    }

    override fun onBindViewHolder(dogViewHolder: DogViewHolder, position: Int) {
        val dog = getItem(position)
        dogViewHolder.bind(dog)
    }

    inner class DogViewHolder(private val binding: DogListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dog: Dog) {

            if (dog.inCollection) {

                binding.dogImage.isVisible = true
                binding.dogIndex.isVisible = false
                binding.dogListItemLayout.background =
                    ContextCompat.getDrawable(
                        binding.dogImage.context,
                        R.drawable.dog_list_item_background
                    )

                binding.dogListItemLayout.setOnClickListener {
                    onItemClickListener?.invoke(dog)
                }
                binding.dogImage.load(dog.imageUrl)
            } else {
                binding.dogImage.isVisible = false
                binding.dogIndex.isVisible = true
                binding.dogIndex.text = dog.index.toString()
                binding.dogListItemLayout.background =
                    ContextCompat.getDrawable(
                        binding.dogImage.context,
                        R.drawable.dog_list_item_null_background
                    )
            }
        }
    }
}