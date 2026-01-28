import { Router } from 'express';
import * as transactionController from '../controllers/transactionController';
import { authenticate } from '../middleware/auth';
import { apiLimiter } from '../middleware/rateLimiter';

const router = Router();

router.use(apiLimiter, authenticate);

router.get('/', transactionController.getTransactions);
router.get('/:id', transactionController.getTransaction);
router.post('/', transactionController.createTransaction);

export default router;
