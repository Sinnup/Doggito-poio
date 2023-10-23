package com.fruse.dogedex.dogList

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.fruse.dogedex.dogDetail.DogDetailActivity.Companion.DOG_KEY
import com.fruse.dogedex.dogDetail.DogDetailComposeActivity
import com.fruse.dogedex.core.ui.theme.DogedexTheme
import com.fruse.dogedex.core.model.Dog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DogListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            DogedexTheme {

                DogListScreen(
                    onNavigationIconClick = ::onNavigationIconClick,
                    onDogClicked = ::openDogDetailActivity
                )
            }
        }
//        val binding = ActivityDogListBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val loadingWheel = binding.loadingWheel
//
//        val recycler = binding.dogRecycler
//        recycler.layoutManager = GridLayoutManager(this, GRID_SPAN_COUNT)
//
//        val adapter = DogAdapter()
//
//        adapter.setOnItemClickListener {
//            // Pasar el dog a DogDetailActivity
//            val intent = Intent(this, DogDetailComposeActivity::class.java)
//            intent.putExtra(DOG_KEY, it)
//            startActivity(intent)
//        }
//
//        recycler.adapter = adapter
//
//        dogListViewModel.dogList.observe(this) { dogList ->
//            Log.i("test", dogList.toString())
//            adapter.submitList(dogList)
//        }
//
//        dogListViewModel.status.observe(this) { status ->
//            when (status) {
//                is ApiResponseStatus.Error -> {
//                    loadingWheel.isVisible = false
//                    Toast.makeText(this, status.messageId, Toast.LENGTH_SHORT).show()
//                }
//
//                is ApiResponseStatus.Loading -> loadingWheel.isVisible = true
//                is ApiResponseStatus.Success -> loadingWheel.isVisible = false
//            }
//        }
    }

    private fun openDogDetailActivity(dog: Dog) {
        val intent = Intent(this, DogDetailComposeActivity::class.java)
        intent.putExtra(DOG_KEY, dog)
        startActivity(intent)
    }

    private fun onNavigationIconClick() {
        finish()
    }
}