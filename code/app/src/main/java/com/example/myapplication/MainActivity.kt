package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Vector7Block(
                            modifier = Modifier.padding(
                                start = 78.dp,
                                top = 599.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Vector7Block(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(340.dp)
            .height(280.01776.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.vector_7),
            contentDescription = "vector 7",
            contentScale = ContentScale.None
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVector7Block() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Vector7Block(
                modifier = Modifier.padding(
                    start = 200.dp,
                    top = 650.dp
                )
            )
        }
    }
}
