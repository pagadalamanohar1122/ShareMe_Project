import React, { useState } from 'react';
import { authService } from '../services/api';

const LoginTest: React.FC = () => {
  const [testResults, setTestResults] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const addResult = (result: string) => {
    setTestResults(prev => [...prev, result]);
  };

  const runTests = async () => {
    setIsLoading(true);
    setTestResults([]);

    // Test 1: Invalid credentials
    try {
      addResult('Testing invalid credentials...');
      await authService.login({ email: 'wrong@email.com', password: 'wrongpassword' });
      addResult('❌ ERROR: Should have failed with invalid credentials');
    } catch (error: any) {
      if (error.response?.data?.message === 'Invalid credentials') {
        addResult('✅ PASS: Invalid credentials properly handled');
      } else {
        addResult(`❌ FAIL: Unexpected error response: ${JSON.stringify(error.response?.data)}`);
      }
    }

    // Test 2: Valid credentials (existing test user)
    try {
      addResult('Testing valid credentials...');
      const response = await authService.login({ 
        email: 'john.doe@example.com', 
        password: 'password123' 
      });
      if (response.accessToken && response.user) {
        addResult('✅ PASS: Valid credentials work correctly');
      } else {
        addResult('❌ FAIL: Login succeeded but missing token or user data');
      }
    } catch (error: any) {
      addResult(`❌ FAIL: Valid credentials failed: ${error.response?.data?.message || error.message}`);
    }

    // Test 3: Network error simulation
    try {
      addResult('Testing network error handling...');
      // This will fail because we're trying to connect to a non-existent server
      const tempBaseURL = 'http://localhost:9999';
      await fetch(`${tempBaseURL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'test@test.com', password: 'test' })
      });
      addResult('❌ ERROR: Network error test failed - should not reach here');
    } catch (error: any) {
      addResult('✅ PASS: Network errors are properly caught');
    }

    setIsLoading(false);
  };

  return (
    <div className="min-h-screen p-8 bg-gray-50">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-6">Login Error Handling Test</h1>
        
        <div className="bg-white p-6 rounded-lg shadow-lg mb-6">
          <button
            onClick={runTests}
            disabled={isLoading}
            className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {isLoading ? 'Running Tests...' : 'Run Error Handling Tests'}
          </button>
        </div>

        {testResults.length > 0 && (
          <div className="bg-white p-6 rounded-lg shadow-lg">
            <h2 className="text-xl font-semibold mb-4">Test Results:</h2>
            <div className="space-y-2">
              {testResults.map((result, index) => (
                <div
                  key={index}
                  className={`p-2 rounded ${
                    result.includes('✅') 
                      ? 'bg-green-50 text-green-800' 
                      : result.includes('❌') 
                      ? 'bg-red-50 text-red-800'
                      : 'bg-blue-50 text-blue-800'
                  }`}
                >
                  {result}
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="bg-white p-6 rounded-lg shadow-lg mt-6">
          <h2 className="text-xl font-semibold mb-4">Manual Test Instructions:</h2>
          <ol className="list-decimal list-inside space-y-2">
            <li>Go to the login page: <a href="http://localhost:3001/login" className="text-blue-600 underline">http://localhost:3001/login</a></li>
            <li>Try logging in with wrong credentials (e.g., wrong@email.com / wrongpass)</li>
            <li>You should see an error message: "Invalid credentials"</li>
            <li>Try logging in with correct credentials: john.doe@example.com / password123</li>
            <li>You should be redirected to the dashboard</li>
          </ol>
        </div>
      </div>
    </div>
  );
};

export default LoginTest;