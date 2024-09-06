import unittest
from main import app

class TestSystem(unittest.TestCase):

    def setUp(self):
        self.app = app.test_client()
        self.app.testing = True

    def test_home_route(self):
        result = self.app.get('/')
        self.assertEqual(result.status_code, 200)
        self.assertEqual(result.data, b'API is running')

if __name__ == '__main__':
    unittest.main()
