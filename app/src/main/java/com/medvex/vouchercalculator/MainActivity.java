package com.medvex.vouchercalculator;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private DecimalFormat floatFormatter = new DecimalFormat("#.##");
	private LayoutInflater inflater;

	private ViewGroup billLayout;
	private ViewGroup voucherLayout;

	private Button billAddButton;
	private Button voucherAddButton;

	private TextWatcher billTextWatcher;
	private TextWatcher voucherTextWatcher;

	private OnClickListener addBillOnClickListener;
	private OnClickListener addVoucherOnClickListener;
	private OnClickListener removeBillOnClickListener;
	private OnClickListener removeVoucherOnClickListener;
	private OnClickListener checkboxVoucherOnClickListener;

	private TextView summary;
	private TextView billHeader;
	private TextView voucherHeader;

	private Float billTotalValue = 0f;
	private Float voucherTotalValue = 0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		initItems();

		billAddButton.setOnClickListener(addBillOnClickListener);
		voucherAddButton.setOnClickListener(addVoucherOnClickListener);

		updateBillHeader();
		updateVoucherHeader();

		addVoucherItem();
		addBillItem();

	}

	private void addBillItem() {
		final View newBillItem = inflater.inflate(R.layout.bill_item, billLayout, false);

		EditText newBillEditor = (EditText) newBillItem.findViewById(R.id.edit_bill_value);
		Button newBillRemoveButton = (Button) newBillItem.findViewById(R.id.button_remove_bill);

		newBillRemoveButton.setOnClickListener(removeBillOnClickListener);

		newBillEditor.addTextChangedListener(billTextWatcher);
		newBillEditor.requestFocus();

		billLayout.addView(newBillItem);
		updateAllItems();
	}

	private void addVoucherItem() {
		final View newVoucherItem = inflater.inflate(R.layout.voucher_item, voucherLayout, false);

		EditText newVoucherValueEditor = (EditText) newVoucherItem.findViewById(R.id.edit_voucher_value);
		EditText newVoucherAmountEditor = (EditText) newVoucherItem.findViewById(R.id.edit_voucher_amount);
		Button newVoucherRemoveButton = (Button) newVoucherItem.findViewById(R.id.button_remove_voucher);
		CheckBox newVoucherCheckBox = (CheckBox) newVoucherItem.findViewById(R.id.checkbox_auto);

		newVoucherRemoveButton.setOnClickListener(removeVoucherOnClickListener);
		newVoucherCheckBox.setOnClickListener(checkboxVoucherOnClickListener);

		newVoucherAmountEditor.setText("1");
		newVoucherValueEditor.addTextChangedListener(voucherTextWatcher);
		newVoucherAmountEditor.addTextChangedListener(voucherTextWatcher);
		newVoucherValueEditor.requestFocus();

		voucherLayout.addView(newVoucherItem);

		calculateDynamicVoucher();
		updateVoucherTotalValue();
		updateAllItems();
	}

	private void removeBillItem(View view) {
		RelativeLayout billItemLayout = (RelativeLayout) view.getParent();

		billLayout.removeView(billItemLayout);

		if (billLayout.getChildCount() == 0) {
			addBillItem();
		}

		updateBillTotalValue();
		calculateDynamicVoucher();
		updateVoucherTotalValue();
		updateAllItems();
	}

	private void removeVoucherItem(View view) {
		RelativeLayout voucherItemLayout = (RelativeLayout) view.getParent();

		voucherLayout.removeView(voucherItemLayout);

		if (voucherLayout.getChildCount() == 0) {
			addVoucherItem();
		}

		calculateDynamicVoucher();
		updateVoucherTotalValue();
		updateAllItems();
	}

	private void updateSummary() {
		Float balance = voucherTotalValue - billTotalValue;
		if (balance < 0) {
			summary.setText(String.format(getResources().getString(R.string.underpay), floatFormatter.format(Math.abs(balance))));
		} else {
			summary.setText(String.format(getResources().getString(R.string.overpay), floatFormatter.format(Math.abs(balance))));
		}
	}

	private void updateBillButtonsEnabled() {
		Boolean addButtonEnabled = true;
		Integer childCount = billLayout.getChildCount();

		for (int i = 0; i < childCount; i++) {
			EditText billValue = (EditText) billLayout.getChildAt(i).findViewById(R.id.edit_bill_value);
			Button billDeleteButton = (Button) billLayout.getChildAt(i).findViewById(R.id.button_remove_bill);
			Float floatBillValue = getEditTextAsFloat(billValue);

			if (floatBillValue == 0f) {
				addButtonEnabled = false;
			}

			if (childCount == 1) {
				if (floatBillValue == 0f) {
					billDeleteButton.setEnabled(false);
				} else {
					billDeleteButton.setEnabled(true);
				}
			} else {
				billDeleteButton.setEnabled(true);
			}

		}
		billAddButton.setEnabled(addButtonEnabled);
	}

	private void updateFirstBillDeleteButtonSymbol() {
		Integer childCount = billLayout.getChildCount();
		if (childCount > 0) {
			Button button = (Button) billLayout.getChildAt(0).findViewById(R.id.button_remove_bill);
			if (childCount == 1) {
				button.setText(R.string.clear_sign);
			} else {
				button.setText(R.string.minus_sign);
			}
		}
	}

	private void updateBillHeader() {
		if (billTotalValue == 0) {
			billHeader.setText(getResources().getString(R.string.zero_bill));
		} else if (billTotalValue > 0) {
			billHeader.setText(String.format(getResources().getString(R.string.nonzero_bill), floatFormatter.format(billTotalValue)));
		}
	}

	private void updateBillTotalValue() {
		Float tempValue = 0f;
		for (int i = 0; i < billLayout.getChildCount(); i++) {
			EditText billValue = (EditText) billLayout.getChildAt(i).findViewById(R.id.edit_bill_value);
			tempValue += getEditTextAsFloat(billValue);
		}
		billTotalValue = tempValue;
	}

	private void updateVoucherButtonsEnabled() {
		Boolean addButtonEnabled = true;
		Integer childCount = voucherLayout.getChildCount();
		
		if (childCount == 1) {
			EditText voucherValue = (EditText) voucherLayout.getChildAt(0).findViewById(R.id.edit_voucher_value);
			EditText voucherAmount = (EditText) voucherLayout.getChildAt(0).findViewById(R.id.edit_voucher_amount);
			Button voucherDeleteButton = (Button) voucherLayout.getChildAt(0).findViewById(R.id.button_remove_voucher);
			CheckBox voucherCheckBox = (CheckBox) voucherLayout.getChildAt(0).findViewById(R.id.checkbox_auto);
			
			Float floatVoucherAmount = getEditTextAsFloat(voucherAmount);
			Float floatVoucherValue = getEditTextAsFloat(voucherValue);
			
			if (isCheckboxApplicable(voucherCheckBox)) {
				if(floatVoucherAmount.equals(0f) || !floatVoucherValue.equals(0f)){
					voucherDeleteButton.setEnabled(true);
				}
			} else {
				if (floatVoucherAmount*floatVoucherValue != 0f) {
					voucherDeleteButton.setEnabled(true);
				} else {
					voucherDeleteButton.setEnabled(false);
					addButtonEnabled = false;
				}
			}
			
		} else {
			for (int i = 0; i < childCount; i++) {
				EditText voucherValue = (EditText) voucherLayout.getChildAt(i).findViewById(R.id.edit_voucher_value);
				EditText voucherAmount = (EditText) voucherLayout.getChildAt(i).findViewById(R.id.edit_voucher_amount);
				Button voucherDeleteButton = (Button) voucherLayout.getChildAt(i).findViewById(R.id.button_remove_voucher);
				Float floatVoucherValue = getEditTextAsFloat(voucherValue) * getEditTextAsFloat(voucherAmount);

				if (floatVoucherValue == 0f) {
					addButtonEnabled = false;
				}

				voucherDeleteButton.setEnabled(true);
			}
		}

		voucherAddButton.setEnabled(addButtonEnabled);
	}

	private void updateVoucherAmountsEnabled() {
		Integer childCount = voucherLayout.getChildCount();

		if (childCount == 1) {
			CheckBox voucherCheckBox = (CheckBox) voucherLayout.getChildAt(0).findViewById(R.id.checkbox_auto);
			EditText voucherAmount = (EditText) voucherLayout.getChildAt(0).findViewById(R.id.edit_voucher_amount);

			if (voucherCheckBox.isChecked()) {
				voucherAmount.setEnabled(false);
			} else {
				voucherAmount.setEnabled(true);
			}
			
		} else {
			for (int i = 0; i < childCount; i++) {
				EditText voucherAmount = (EditText) voucherLayout.getChildAt(i).findViewById(R.id.edit_voucher_amount);
				voucherAmount.setEnabled(true);
			}
		}
	}

	private void updateVoucherCheckboxEnabled() {
		Integer childCount = voucherLayout.getChildCount();

		if (childCount == 1) {
			CheckBox voucherCheckBox = (CheckBox) voucherLayout.getChildAt(0).findViewById(R.id.checkbox_auto);
			voucherCheckBox.setVisibility(View.VISIBLE);
		} else {
			for (int i = 0; i < childCount; i++) {
				CheckBox voucherCheckBox = (CheckBox) voucherLayout.getChildAt(i).findViewById(R.id.checkbox_auto);
				voucherCheckBox.setChecked(false);
				voucherCheckBox.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void updateFirstVoucherDeleteButtonSymbol() {
		Integer childCount = voucherLayout.getChildCount();
		if (childCount > 0) {
			Button button = (Button) voucherLayout.getChildAt(0).findViewById(R.id.button_remove_voucher);
			if (childCount == 1) {
				button.setText(R.string.clear_sign);
			} else {
				button.setText(R.string.minus_sign);
			}
		}
	}

	private void updateVoucherHeader() {
		if (voucherTotalValue == 0) {
			voucherHeader.setText(getResources().getString(R.string.zero_vouchers));
		} else if (voucherTotalValue > 0) {
			voucherHeader.setText(String.format(getResources().getString(R.string.nonzero_vouchers), floatFormatter.format(voucherTotalValue)));
		}
	}

	private void updateVoucherTotalValue() {
		Integer childCount = voucherLayout.getChildCount();
		Float tempValue = 0f;

		for (int i = 0; i < childCount; i++) {
			EditText voucherValue = (EditText) voucherLayout.getChildAt(i).findViewById(R.id.edit_voucher_value);
			EditText voucherAmount = (EditText) voucherLayout.getChildAt(i).findViewById(R.id.edit_voucher_amount);
			tempValue += getEditTextAsFloat(voucherValue) * getEditTextAsFloat(voucherAmount);
		}
		voucherTotalValue = tempValue;
	}

	private void updateAllItems() {
		
		updateBillHeader();
		updateFirstBillDeleteButtonSymbol();
		updateBillButtonsEnabled();
			
		updateVoucherHeader();
		updateFirstVoucherDeleteButtonSymbol();
		updateVoucherButtonsEnabled();
		updateVoucherAmountsEnabled();
		updateVoucherCheckboxEnabled();
		
		updateSummary();
	}

	private void processCheckBoxVoucherItem(View view) {
		calculateDynamicVoucher();
		updateVoucherTotalValue();
		updateAllItems();
	}

	private Boolean isCheckboxApplicable(View view) {
		Boolean isEnabled = false;
		CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_auto);
		if (checkBox.isEnabled()) {
			if (checkBox.isChecked()) {
				isEnabled = true;
			}
		}
		return isEnabled;
	}

	private void calculateDynamicVoucher() {
		Integer childCount = voucherLayout.getChildCount();
		Integer newVoucherAmout = 0;
		
		if (childCount == 1) {
			View firstVoucher = (View) voucherLayout.getChildAt(0);
			if (isCheckboxApplicable(firstVoucher)) {
				EditText voucherValue = (EditText) firstVoucher.findViewById(R.id.edit_voucher_value);
				Float voucherFloatValue = getEditTextAsFloat(voucherValue);
				
				if (!voucherFloatValue.equals(0f)) {
					Float percentAdjustedInitialValue = billTotalValue + (billTotalValue * 0.07f);
					Integer timesFit = (int) Math.floor(percentAdjustedInitialValue / voucherFloatValue);
					newVoucherAmout = timesFit;
					
					if (timesFit*voucherFloatValue>billTotalValue) {
						Float temp = timesFit*voucherFloatValue - billTotalValue;
						temp = temp/voucherFloatValue;
						newVoucherAmout = (int) Math.floor(newVoucherAmout-temp);
					}
					
				}
				setVoucherAmount(0, newVoucherAmout);
			}	
		}
	}

	private void setVoucherAmount(Integer position, Integer amount) {
		if (amount < 1000){
			EditText voucherAmount = (EditText) voucherLayout.getChildAt(position).findViewById(R.id.edit_voucher_amount);
			voucherAmount.removeTextChangedListener(voucherTextWatcher);
			voucherAmount.setText(Integer.toString(amount));
			voucherAmount.addTextChangedListener(voucherTextWatcher);
		} else {
			Toast.makeText(getApplicationContext(), "Too many vouchers :)", Toast.LENGTH_SHORT).show();
		}		
	}

	private Float getEditTextAsFloat(EditText et) {
		String str = et.getText().toString();
		Float val;

		if (str == null || str.isEmpty() || str.equals(".")) {
			val = 0f;
		} else {
			str = str.replace(',', '.');
			val = Float.valueOf(str);
		}

		return val;
	}

	private void initItems() {
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		billLayout = (ViewGroup) findViewById(R.id.bill_items);
		voucherLayout = (ViewGroup) findViewById(R.id.voucher_items);

		billAddButton = (Button) findViewById(R.id.button_add_bill);
		voucherAddButton = (Button) findViewById(R.id.button_add_voucher);

		billTextWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				updateBillTotalValue();
				calculateDynamicVoucher();
				updateVoucherTotalValue();
				updateAllItems();
				
			}
		};
		voucherTextWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				calculateDynamicVoucher();
				updateVoucherTotalValue();
				updateAllItems();
			}
		};

		addBillOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				addBillItem();
			}
		};
		addVoucherOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				addVoucherItem();
			}
		};
		removeBillOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				removeBillItem(view);
			}
		};
		removeVoucherOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				removeVoucherItem(view);
			}
		};
		checkboxVoucherOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				processCheckBoxVoucherItem(view);
			}
		};

		summary = (TextView) findViewById(R.id.text_summary);
		billHeader = (TextView) findViewById(R.id.text_info_bill);
		voucherHeader = (TextView) findViewById(R.id.text_info_vouchers);

	}
}
