import { Router } from 'express';
import * as reminderController from '../controllers/reminderController';
import { authenticate } from '../middleware/auth';

const router = Router();

router.use(authenticate);

router.get('/', reminderController.getReminders);
router.post('/', reminderController.createReminder);
router.patch('/:id', reminderController.updateReminder);
router.patch('/:id/read', reminderController.markReminderAsRead);
router.delete('/:id', reminderController.deleteReminder);

export default router;
