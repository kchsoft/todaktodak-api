#!/bin/bash

# 결과를 저장할 배열 선언
declare -a build_times

# 5번의 빌드 실행
for i in {1..5}; do
    echo "Build #$i 실행 중..."
    
    # 빌드 시작 시간 기록 (초 단위로)
    start_time=$(date +%s)
    
    # Gradle clean build 실행
    ./gradlew clean build
    
    # 빌드 종료 시간 기록 (초 단위로)
    end_time=$(date +%s)
    
    # 실행 시간 계산
    build_time=$((end_time - start_time))
    
    # 배열에 실행 시간 저장
    build_times[$i]=$build_time
    
    echo "Build #$i 완료: ${build_time}초"
    echo "-------------------"
    
    # 다음 빌드 전 잠시 대기 (시스템 안정화)
    sleep 2
done

# 평균 시간 계산
total=0
for time in "${build_times[@]}"; do
    total=$((total + time))
done
average=$((total / 5))

# 결과 출력
echo
echo "빌드 시간 요약:"
echo "----------------"
for i in {1..5}; do
    echo "$i번째 빌드: ${build_times[$i]}초"
done
echo "----------------"
echo "평균 빌드 시간: ${average}초"
