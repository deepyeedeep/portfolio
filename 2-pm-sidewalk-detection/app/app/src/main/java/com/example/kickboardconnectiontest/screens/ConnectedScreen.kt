package com.example.kickboardconnectiontest.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kickboardconnectiontest.R

val appleneot = FontFamily(
    Font(R.font.applesdgothicneot, FontWeight.Normal)
)
// ReadyScreen 함수 내에서 NanumSquareNeo 글꼴을 사용하는 Text 스타일 정의
val applenotTextStyle = androidx.compose.ui.text.TextStyle(
    fontFamily = appleneot,
    fontSize = 24.sp, // 원하는 크기로 변경하세요
    fontWeight = FontWeight.Normal
)


@Composable
@Preview
fun ConnectedScreen(
    disconnectFunc: () -> Unit = {},
    penPoint: String = "0"
){
    var isDialogCalled by remember { mutableStateOf(false) }
    if(isDialogCalled){
        AlertDialog(
            onDismissRequest = { /* Dismiss */ },
            title = { Text("반납 확인") },
            text = { Text("킥보드를 반납하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = disconnectFunc) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // Dismiss
                }) {
                    Text("Cancel")
                }
            }
        )

    }


    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = "Background Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop // 화면 비율에 맞게 이미지 조정
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = penPoint,
            modifier = Modifier.padding(15.dp),
            fontFamily = appleneot, // Apply NanumGothic font ,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.85f) // Set text color to white
        )

        Button(
            onClick = { isDialogCalled = true},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha=0.6f) // 투명한 검은색 배경 설정
            ),
            border = BorderStroke(1.dp, Color.White) // 하얀색 외곽선 설정
        ){
            Text(
                text = "RETURN",
                fontSize = 18.sp,
                )
        }
    }
}