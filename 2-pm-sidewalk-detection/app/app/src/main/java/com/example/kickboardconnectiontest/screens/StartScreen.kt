package com.example.kickboardconnectiontest.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kickboardconnectiontest.R


val nanumSquareNeo = FontFamily(
    Font(R.font.nanumsquareneo, FontWeight.Normal)
)
// ReadyScreen 함수 내에서 NanumSquareNeo 글꼴을 사용하는 Text 스타일 정의
val nanumSquareTextStyle = androidx.compose.ui.text.TextStyle(
    fontFamily = nanumSquareNeo,
    fontSize = 24.sp, // 원하는 크기로 변경하세요
    fontWeight = FontWeight.Normal
)

@Composable
fun StartScreen() {
    var isVisible by remember { mutableStateOf(false) }

    // Trigger visibility change after a delay
    Box(modifier = Modifier
        .fillMaxSize()) {
        // 배경 이미지 추가

        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // 화면 비율에 맞게 이미지 조정
        )
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Image",
            modifier = Modifier
                .size(150.dp) // 로고 이미지의 크기를 설정
                .align(Alignment.TopCenter) // 로고 이미지를 화면 중앙에 배치
                .padding(top = 50.dp), // 필요한 경우 패딩 조정
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.87f)) // 투명도 조절
        )

        // 텍스트 추가
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp), // 상단 정렬
            verticalArrangement = Arrangement.spacedBy(16.dp), // 텍스트 간격 조정
        ) {
            LaunchedEffect(Unit) {
                // Delay to simulate some loading or condition
                kotlinx.coroutines.delay(500) // Adjust delay as needed
                isVisible = true
            }

            // Text composable with fadeIn animation
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(initialAlpha = 0.3f) // Adjust initialAlpha if needed
            )  {
                Text(
                    text = "안전한",
                    modifier = Modifier
                        .padding(bottom = 8.dp, start = 55.dp, top = 60.dp) // Add top margin
                        .align(Alignment.Start), // Align to the left
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontFamily = nanumSquareNeo, // Apply NanumGothic font
                        fontSize = 42.sp,
                        color = Color.White.copy(alpha = 0.85f) // Set text color to white
                    )
                )
            }
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(initialAlpha = 0.3f) // Adjust initialAlpha if needed
            )  {
                Text(
                    text = "공유 PM",
                    modifier = Modifier
                        .padding(bottom = 8.dp, start = 55.dp, top = 20.dp) // Add top margin
                        .align(Alignment.Start), // Align to the left
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontFamily = nanumSquareNeo, // Apply NanumGothic font
                        fontSize = 42.sp,
                        color = Color.White.copy(alpha = 0.85f) // Set text color to white
                    )
                )
            }
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(initialAlpha = 0.3f) // Adjust initialAlpha if needed
            )  {
                Text(
                    text = "환경을 위해",
                    modifier = Modifier
                        .padding(bottom = 12.dp, start = 55.dp, top = 20.dp) // Add top margin
                        .align(Alignment.Start), // Align to the left
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontFamily = nanumSquareNeo, // Apply NanumGothic font
                        fontSize = 42.sp,
                        color = Color.White.copy(alpha = 0.85f) // Set text color to white
                    )
                )
            }
        }
    }
}
