package com.example;
public class Solution {
	public int solution(String S) {
// Remove leading zeros first (optional but safe)
		int start = 0;
		while (start < S.length() && S.charAt(start) == '0') {
			start++;
		}
		if (start == S.length())
			return 0; // zero value

		int steps = 0;
		int end = S.length() - 1;

// Iterate from right to left (excluding MSB)
		for (int i = end; i > start; i--) {
			if (S.charAt(i) == '0') {
				steps += 1; // divide by 2
			} else {
				steps += 2; // subtract 1 + divide by 2
			}
		}

// Final step for the MSB (the highest '1')
		steps += 1;

		return steps;
	}

	public static void main(String[] args) {
		Solution sol = new Solution();
		System.out.println(sol.solution("011100")); // Expected 7
		System.out.println(sol.solution("111")); // Expected 5
		System.out.println(sol.solution("1111010101111")); // Expected 22
		System.out.println(sol.solution("1".repeat(400000))); // Expected 799999
	}
}
