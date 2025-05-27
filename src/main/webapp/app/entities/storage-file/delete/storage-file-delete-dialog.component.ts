import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IStorageFile } from '../storage-file.model';
import { StorageFileService } from '../service/storage-file.service';

@Component({
  templateUrl: './storage-file-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class StorageFileDeleteDialogComponent {
  storageFile?: IStorageFile;

  protected storageFileService = inject(StorageFileService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.storageFileService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
