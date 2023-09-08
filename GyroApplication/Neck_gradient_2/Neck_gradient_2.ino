#include <SoftwareSerial.h>
#include "Wire.h"
#include <MPU6050_light.h>

//#define blueRx 2   // 우노보드 Rx   
//#define blueTx 3   // 우노보드 Tx  
#define blueRx 4   // 우노보드 Rx   
#define blueTx 5   // 우노보드 Tx
   
#define biv_motor 11   // 진동모터

SoftwareSerial BT_Ble(blueRx, blueTx);  
MPU6050 mpu(Wire);
unsigned long timer = 0;

unsigned long Now_Millis = 0; // 내부 클럭을 이용한 millis() 함수사용.
unsigned long Start_Millis_Temp = 0;  //
unsigned long Start_Millis_17 = 0;  // 17도 이상일때 시작되는 시간, (1분이상인지 확인)
int status_over = 0;                // 정상일경우 0, 타이머 작동 시 1, 17보다 클 경우 2;   

unsigned long Start_Millis_10minute = 0; // 10분이 지났는지 확인하기 위한 시간함수.

int angle_x = 0;  // X 축의 각도
int angle_y = 0;  // Y 축의 각도
int angle_z = 0;  // Z 축의 각도

int average_x = 0;  // X 축의 각도의  평균값 ( sum_angle_x / count_Num)
float sum_angle_x = 0; // X 축의 각도의 합계. (10분간 합계)
int count_Num = 0; // sum 한 갯수 (int sum_angle_x )

int xm_12 = 1; // 실기간 각도와 평균은 순차적으로 전송   (1:실시간 각도  2: 평균값)

void setup() {

  // Open serial communications and wait for port to open
  Serial.begin(9600);
    while (!Serial) { ;} // wait for serial port to connect. 
                    
  BT_Ble.begin(9600);
    while (!BT_Ble) { ;} // wait for serial3 port to connect. 

  Wire.begin();
  
  byte status = mpu.begin();
  
  while(status!=0){ } // stop everything if could not connect to MPU6050
  
  
  delay(1000);
  
  mpu.calcOffsets(); // gyro and accelero
  //mpu.setGyroOffsets();
  
  pinMode(biv_motor, OUTPUT); // 진동모터
  
  Time_delay(300);
  Serial.println("Done!\n");
  
 }

void loop() { // run over and over

           Now_Millis = millis();
           getAngle_MPU6050();// X축 기울기 각도를 읽어서 전역변수(angle_x)에 넣는다.
          if(( Now_Millis - Start_Millis_10minute )>= 600000)  // 10분이후 초기화
               {
                  Serial.println("10분 지났음");
                  count_Num = 0;
                  sum_angle_x = 0;
                  average_x = 0;
                  Start_Millis_10minute = Now_Millis;
               }             
          if(( Now_Millis - Start_Millis_Temp )>= 500) // 0.5초 
            {
//               getAngle_MPU6050();// X축 기울기 각도를 읽어서 전역변수(angle_x)에 넣는다.
                
               if(angle_x >= 17)  //17도 보다 클경우. 
                 {
                   
                   if (status_over == 0 ) 
                      {
                        Serial.println("기울기 17이상, 처음으로");
                        status_over=1;
                        Start_Millis_17 = Now_Millis; // 17도 보다 클경우에는 현재시간을 설정.

                      }
                   
                   else if ((status_over >= 1) &&  (Now_Millis-Start_Millis_17 >= 60000)) 
                      {
                        Serial.println("기울기 17 이상, 1분이상 지속 됨");
//                        BT_send(3); // 경고문 전송 
                        status_over=2;
                        Biv_Motot();
//                        Start_Millis_17 = Now_Millis; // 경고문을 보낸후 현재시간을 설정.
                      }
                   else { 
                         Serial.println("기울기 17 이상, 1분 미만 임");
                         } // 정상진행.                                             
                  }
               else if(angle_x < 17)  //17도 보다 작을경우. 
                  {
                        Serial.println("기울기 17 미만임, 좋은자세 임");
                        status_over = 0;
                        Start_Millis_17 = Now_Millis; // 17도 보다 작을경우에는 현재시간을 설정.
                   }
                   
              else{
                     Serial.println("기타자세 임");

                     }


                count_Num = count_Num +1;
                sum_angle_x = sum_angle_x + angle_x; // 지금까지의 X축 각도의 합계                
                average_x = sum_angle_x/count_Num ;  
                                
//                Serial.print("count_Num = "); Serial.println(count_Num);
                Serial.print("angle_x = "); Serial.println(angle_x);
//                Serial.print("average_x = ");                Serial.print("sum_angle_x = "); Serial.println(sum_angle_x);
                Serial.println(average_x);

                BT_send(status_over);
                Start_Millis_Temp = Now_Millis;  // 지금의 시간을 재시작하는 시간으로 변경을 한다.          
      
        } // end_ if(( Now_Millis - Start_Millis_Temp )>= 500) // 0.5초마다 초음파 신호 송신(물체 체크).
     delay(10);
    } // end Loop()

// MPU 6050 센서로 부터 각도를 측정함.
void getAngle_MPU6050() 
 {
  mpu.update();

  //angle_y = int(mpu.getAngleX());
  angle_x = int(mpu.getAngleY());
  //angle_z = int(mpu.getAngleZ());
  //angle_y = -1*(int(mpu.getAngleX()));
  //angle_x = -1*(int(mpu.getAngleY()));
  //angle_z = -1*(int(mpu.getAngleZ()));
  }

// 숫자의 자리수를 1,000cm로 맞춰서 보냄. 500cm가 최고 임.)
void BT_send(int status_over ) // error code - 0: 정상,  1: 1분이상 17~20도, 2:1분이상 20도 이상
{

 BT_Ble.print(String(angle_x)+","+String(average_x)+","+String(status_over)); //블루투스 모듈로 송신
}

void Biv_Motot()// 진동모터 동작.
      {
         digitalWrite(biv_motor, HIGH);
         Time_delay(500);
         digitalWrite(biv_motor, LOW);
         Time_delay(500); 
      }
      
void Time_delay(int count)
{
   unsigned long Start_Millis_Temp2 = millis();
   while(1)
   {
     Now_Millis = millis();
     if(( Now_Millis - Start_Millis_Temp2 )> count) 
      {
        break;
      }//end_if(( Now_Millis - Start_Millis_Temp2 )> count)
     
     //for( int i=0; i<1000; i++)
     //   {
     //     int j= j + 1;
      //  }
   }//end_while(1)
}
