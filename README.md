
<center><b>J-Distribute</b>는 서버에 파일의 백업 & 배포를 지원하는 프로그램 입니다.</center>

CI 구축이 어려운 곳에서 서버 없이 간단하게 사용할 수 있으며, excel로 특정 파일만 지정하여 백업 & 배포 합니다.

![j-distribute screen shot](https://lahuman.github.io/assets/project/jdistribute/jdistribute.PNG)

<div markdown="0"><a href="https://github.com/lahuman/J-Distribute" class="btn btn-warning">Source 바로가기</a></div>

## Download

### Template 파일들 
<div markdown="0"><a href="https://lahuman.github.io/assets/project/jdistribute/template.xls" class="btn btn-info">Excel Template</a></div>

<div markdown="0"><a href="https://lahuman.github.io/assets/project/jdistribute/SERVER_INFO.xml" class="btn btn-info">Shell Template</a></div>


### Windows 전용 실행 파일

<div markdown="0"><a href="https://lahuman.github.io/assets/project/jdistribute/J-DISTRIBUTE_32.zip" class="btn btn-success">J-Distribute 32bit</a></div>

<div markdown="0"><a href="https://lahuman.github.io/assets/project/jdistribute/J-DISTRIBUTE_64.zip" class="btn btn-success">J-Distribute 64bit</a></div>

### Linux 64bit 전용 실행 파일
<div markdown="0"><a href="https://lahuman.github.io/assets/project/jdistribute/J-Distribute_linux_64.jar" class="btn btn-success">J-Distribute Linux 64bit</a></div>

#### Linux 실행 방법

``` bash
java -jar J-Distribute_linux_64.jar
```


### 다른 OS의 경우 Source를 다운받아 실행 하셔야 합니다.

## Notice

* JRE 1.7 이상이 설치 되어 있어야 합니다.
* JRE_HOME 환경 변수가 설정 되어 있어야 합니다.
    * 다음이 path에 추가 되어야 함 
		* $JRE_HOME$\bin
* <a href="https://lahuman.github.io/assets/project/jdistribute/template.xls">Excel 양식</a>을 이용하여야 합니다.
* 특정 폴더 밑의 모든 파일에 대한 backup, upload를 하기 위해서는 * 를 사용 합니다.
	* /2017/* 
* 파일 배포 전, 후 shell 처리가 가능 합니다.

## Function
* FTP, SCP 기반 파일 업로드
* 업로드 이전에 로컬 디스크에 백업 지원
* 배포 이전, 이후 Shell 실행 지원
    * 예) WAS 재기동
* 설정 파일 저장 및 불러오기

## Update history

* 2015.06.30
    * Before, After Shell 실행 기능 추가
* 2015.04.01
    * SCP 기능 추가
* 2010.07.22
    * 최초 배포



## License

J-Distribute 는 open source 프로그램으로 MIT 라이선스를 따릅니다.

This J-Distribute is free and open source software, distributed under the MIT License. So feel free to use this program on your project without linking back to me or including a disclaimer.
