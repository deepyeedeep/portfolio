package com.example.kickboardconnectiontest.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.kickboardconnectiontest.R

val AppleSDGothicNeoT = FontFamily(
    Font(R.font.applesdgothicneot, FontWeight.Normal)
)
// ReadyScreen 함수 내에서 NanumSquareNeo 글꼴을 사용하는 Text 스타일 정의
val AppleGothicTextStyle = androidx.compose.ui.text.TextStyle(
    fontFamily = AppleSDGothicNeoT,
    fontSize = 24.sp, // 원하는 크기로 변경하세요
    fontWeight = FontWeight.Normal
)



@Composable
fun ReadyScreen(
    /* callback lambda for button.onClick*/
    startConnection: () -> Unit = {},
    sequenceRunning: Boolean = false,
    /* Signal from Repo. */
    errorMessage:String = "",
    /* TextField value */
    nick: String = "",
    setNick: (String) -> Unit,
    device: String = "",
    setDevice: (String) -> Unit,
){
    var isErrDialogOpen by remember {
        mutableStateOf(false)
    }
    val errList = remember{
        mutableListOf<String>()
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = errorMessage, key2 = errList) {
        // Exception 발생하면 errList에 추가, errList가 NotEmpty면 Dialog open
        if(errorMessage.isNotEmpty() and !errList.contains(errorMessage)) {
            // 중복 errorMessage는 추가하지 않음
            errList.add(errorMessage)
        }
        if(errList.isNotEmpty()){
            isErrDialogOpen = true
        }
    }

    if(isErrDialogOpen){
        AlertDialog(
            title = { Text("Error") },
            text = { LazyColumn{items(errList){Text(it)}} },
            onDismissRequest = { /*TODO*/ },
            confirmButton = {
                TextButton(
                    onClick = {
                        errList.clear()
                        isErrDialogOpen = false
                    }
                ) { Text("OK") } },
            icon = {Icon(Icons.Rounded.Warning, contentDescription = null)},
            shape = RoundedCornerShape(10.dp),
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            )
        )
    }

    Box(modifier = Modifier) {
        // 배경 이미지 추가

        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // 화면 비율에 맞게 이미지 조정
        )

        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "Logo Image",
            modifier = Modifier
                .size(200.dp) // 로고 이미지의 크기를 설정
                .align(Alignment.Center) // 로고 이미지를 화면 중앙에 배치
                .offset(y = (-80).dp), // Move the image upwards by 16 dp

            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.87f)) // 투명도 조절

        )

        // 입력 필드 및 버튼 추가
        Column(
            modifier = Modifier
                .padding(bottom = 40.dp) // 필요한 경우 패딩 조정
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField( // NICKNAME TEXTFIELD
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(Color.White), // 배경을 투명한 검은색으로 설정
                label = { Text("User Nickname")}, // 투명도 설정
                value = nick,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                onValueChange = {
                    setNick(it)
                },
                enabled = !sequenceRunning
            )
            TextField(
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .background(Color.White), // 배경을 투명한 검은색으로 설정
                label = { Text("Device Number")},
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                value = device,
                onValueChange = {
                    if(device.length <= 6){
                        setDevice(it)
                    }
                    if(device.length == 6){
                        keyboardController?.hide()
                    }

                },
                enabled = !sequenceRunning
            )
            Button(
                onClick = startConnection,
                enabled = !sequenceRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.50f) // 투명한 검은색 배경 설정
                ),
                border = BorderStroke(1.dp, Color.White) // 하얀색 외곽선 설정
            ) {
                Text(text = "CONNECT", color = Color.White, fontSize = 20.sp,
                    fontFamily = AppleSDGothicNeoT, // Apply NanumGothic font
                    fontWeight = FontWeight.Bold // Make the text bold
                ) // 텍스트 색상 및 투명도 설정
            }
            // Spacer를 추가하여 텍스트 필드 아래에 간격을 만듭니다.
            Spacer(modifier = Modifier.height(35.dp))
        }
    }
}
