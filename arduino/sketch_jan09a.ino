/*
This is for the moto navigation.
At first, we use the blabla pin as input(bluetooth),then have the pins: blablabla as output.
Smart phone send some text to arduino by bluetooth
e.g, :
6
Q

numbers mean the time counting down, 6 means we need 6 pins fot led
Q


*/

boolean dirCheckForA = false;
boolean dirCheckForB2I = false;
boolean dirCheckForJ2P = false;
boolean countingTimeEvent =true;
int countingTime =6;
char chr = 'Z';
 int i = 0;

void setup(){
  Serial.begin(9600); // 開啟 Serial port, 通訊速率為 9600 bps
  for (int pin1 = 2 ; pin1 <=13;pin1++){ // 定義digital 2~13 為輸出
  pinMode(pin1, OUTPUT);
  digitalWrite(pin1,HIGH); //確認每個LED燈
 // delay(50);
  digitalWrite(pin1,LOW);
//  delay(50);
  }
  //Serial.println("OK"); // 回傳"OK"
 
}

void serialEvent() {
  while (Serial.available()){
    chr = Serial.read();
    char inputcheck = Serial.read();
    if (inputcheck == '7'){
      countingTime = 7;
    }
    if (inputcheck == '6'){
    countingTimeEvent =true;
    countingTime = 6;
    }
    else if (inputcheck == '5'){
      countingTimeEvent =true;
    countingTime = 5;
    }
    else if (inputcheck == '4'){
      countingTimeEvent =true;
    countingTime = 4;
    }
    else if (inputcheck == '3'){
      countingTimeEvent =true;
    countingTime = 3;
    }
    else if (inputcheck == '2'){
      countingTimeEvent =true;
    countingTime = 2;
    }
    else if (inputcheck == '1'){
      countingTimeEvent =true;
    countingTime = 1;
    }
    else if (inputcheck == '0'){
      countingTimeEvent =true;
    countingTime = 0;
    }
  //    Serial.println(inputcheck); // 回傳"inputcheck"
  }

}

void loop(){
 if(chr != 'Z'){
   for (int dir = 2; dir <= 5; dir++) { 
        digitalWrite(dir, LOW);
	}
   if(chr == 'J')
      digitalWrite(5, HIGH);
   else if(chr == 'K')
      digitalWrite(2, HIGH);
   else if(chr == 'L')
    digitalWrite(4, HIGH);
   else if(chr == 'M')
    digitalWrite(3, HIGH);
   else if(chr == 'N'){
    digitalWrite(5, HIGH);
    digitalWrite(4, HIGH);
   }
   else if(chr == 'O'){
    digitalWrite(2, HIGH);
    digitalWrite(3, HIGH);
   }
    else if(chr == 'P' || chr == 'F'){
    digitalWrite(3, HIGH);
    digitalWrite(4, HIGH);
   }
 }
 /*  //mark ven change 20140112
 if(chr == 'B'||chr == 'C'||chr == 'D'||chr == 'E'||chr == 'F'||chr == 'G'||chr == 'H'||chr == 'I' ){ // 當B~I時 全方向燈號亮
   for (int dir = 2; dir <= 5; dir++) { 
        digitalWrite(dir, HIGH);
       }
   }
*/
  //mark ven change 20140112
if (chr == 'C'|| chr == 'D' || chr == 'E')
  {
   digitalWrite(2, HIGH); 
  }
  
if (chr == 'G'|| chr == 'H' || chr == 'I')
  {
   digitalWrite(5, HIGH); 
  }
  //mark ven change 20140112


   if(chr == 'Q'&&countingTime == 7)
   {
     digitalWrite(4, HIGH);
     digitalWrite(10, HIGH);
   }
    if(countingTimeEvent){
       for (int ct=8;ct<=13;ct++){
            digitalWrite(ct, LOW);
          }
      // Serial.print(countingTime);
      //
       for(int i  = 0 ;i < 2500;i++){
        for (int ct=0;ct<=(6-countingTime);ct++){
            digitalWrite(ct+7, HIGH);
         }
       }
     if(countingTime == 0)
     for(int i  = 0 ;i < 2500;i++){
        for (int ct=8;ct<=13;ct++){
            digitalWrite(ct, LOW);
          }
     }      
      //countingTimeEvent = false;
     }

    

}
