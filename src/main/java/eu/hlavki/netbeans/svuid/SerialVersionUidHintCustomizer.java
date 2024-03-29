package eu.hlavki.netbeans.svuid;

import java.util.prefs.Preferences;

public class SerialVersionUidHintCustomizer extends javax.swing.JPanel {

    private static final long serialVersionUID = -892296196830595275L;
    private final Preferences pref;

    /**
     * Creates new form SerialVersionUidHintCustomizer
     */
    public SerialVersionUidHintCustomizer(Preferences pref) {
        this.pref = pref;
        initComponents();
        warnForIncorrectValue.setSelected(pref.getBoolean(SerialVersionUidHint.WARN_FOR_INCORRECT_VALUE_KEY,
                SerialVersionUidHint.WARN_FOR_INCORRECT_VALUE_DEFAULT));
        ignoredValuesTextField.setText(pref.get(SerialVersionUidHint.IGNORED_VALUES_KEY, TOOL_TIP_TEXT_KEY));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        warnForIncorrectValue = new javax.swing.JCheckBox();
        ignoredValuesLabel = new javax.swing.JLabel();
        ignoredValuesTextField = new javax.swing.JTextField();

        warnForIncorrectValue.setText(org.openide.util.NbBundle.getMessage(SerialVersionUidHintCustomizer.class, "SerialVersionUidHintCustomizer.warnForIncorrectValue.text")); // NOI18N
        warnForIncorrectValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warnForIncorrectValueActionPerformed(evt);
            }
        });

        ignoredValuesLabel.setText(org.openide.util.NbBundle.getMessage(SerialVersionUidHintCustomizer.class, "SerialVersionUidHintCustomizer.ignoredValuesLabel.text")); // NOI18N

        ignoredValuesTextField.setText(org.openide.util.NbBundle.getMessage(SerialVersionUidHintCustomizer.class, "SerialVersionUidHintCustomizer.ignoredValuesTextField.text")); // NOI18N
        ignoredValuesTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ignoredValuesTextFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(warnForIncorrectValue)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ignoredValuesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoredValuesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(warnForIncorrectValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ignoredValuesLabel)
                    .addComponent(ignoredValuesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void warnForIncorrectValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warnForIncorrectValueActionPerformed
        pref.putBoolean(SerialVersionUidHint.WARN_FOR_INCORRECT_VALUE_KEY, warnForIncorrectValue.isSelected());
    }//GEN-LAST:event_warnForIncorrectValueActionPerformed

    private void ignoredValuesTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ignoredValuesTextFieldKeyReleased
        pref.put(SerialVersionUidHint.IGNORED_VALUES_KEY, ignoredValuesTextField.getText());
    }//GEN-LAST:event_ignoredValuesTextFieldKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ignoredValuesLabel;
    private javax.swing.JTextField ignoredValuesTextField;
    private javax.swing.JCheckBox warnForIncorrectValue;
    // End of variables declaration//GEN-END:variables
}
